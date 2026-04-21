package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.exceptions.InternalTechErrorException;
import com.rmrf.statflux.domain.exceptions.PageIsOutsideOfBoundsException;
import com.rmrf.statflux.domain.exceptions.RefreshInProgressException;
import com.rmrf.statflux.integration.VideoProviderFactory;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.transaction.Transactional;
import com.rmrf.statflux.repository.transaction.TransactionManager;
import com.rmrf.statflux.repository.dto.PaginationStateDto;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ServiceLayerImpl implements ServiceLayer {

    private final LinkRepository linkRepository;
    private final PaginationStateRepository paginationStateRepository;
    private final TransactionManager txManager;
    private final VideoProviderFactory hostingApiFactory;
    private final long refreshDelayMs;
    private final int videosPerPage;

    private final Semaphore refreshSemaphore;

    public ServiceLayerImpl(LinkRepository linkRepository,
        PaginationStateRepository paginationStateRepository, VideoProviderFactory hostingApiFactory,
        long refreshDelayMs, int videosPerPage, TransactionManager txManager) {
        this.linkRepository = linkRepository;
        this.paginationStateRepository = paginationStateRepository;
        this.hostingApiFactory = hostingApiFactory;
        if (refreshDelayMs <= 0) {
            throw new IllegalArgumentException("refreshDelayMs must be positive");
        }

        this.refreshDelayMs = refreshDelayMs;
        if (videosPerPage <= 0) {
            throw new IllegalArgumentException("videosPerPage must be positive");
        }
        this.videosPerPage = videosPerPage;
        this.txManager = txManager;
        refreshSemaphore = new Semaphore(1);
    }

    @Override
    @Transactional
    public @NonNull Result<AddVideoResponse> addVideo(@NonNull String rawUrl) {
        try {
            var hostingApiEither = hostingApiFactory.forUrl(rawUrl);
            if (hostingApiEither.isFailure()) {
                log.error("ServiceLayerImpl[addVideo] no hosting api found for rawUrl={}", rawUrl);
                return hostingApiEither.asFailure().swap();
            }
            var hostingApi = hostingApiEither.get();
            return switch (hostingApi.metadataByLink(rawUrl)) {
                case Success<VideoMetadataResponse> s -> {
                    var dbItem = new LinkDto(
                        null,
                        hostingApi.hostingName(),
                        rawUrl,
                        s.result().id(),
                        s.result().title(),
                        s.result().views(),
                        ZonedDateTime.now(ZoneOffset.UTC)
                    );
                    var success = linkRepository.save(dbItem);
                    if (!success) {
                        log.error("ServiceLayerImpl[addVideo] failed to save rawUrl={}", rawUrl);
                        yield Failure.of(
                            new InternalTechErrorException("failed to save metadata to repo"));
                    }
                    yield Success.of(
                        new AddVideoResponse(
                            hostingApi.hostingName(),
                            s.result().title(),
                            rawUrl,
                            s.result().views()));
                }
                case Failure<VideoMetadataResponse> f -> {
                    log.error("ServiceLayerImpl[addVideo] failed to parse rawUrl={} reason={}",
                        rawUrl,
                        f.exception().getMessage());
                    yield f.swap();
                }
            };
        } catch (Exception e) {
            log.error("ServiceLayerImpl[addVideo] unhandled exception", e);
            return Failure.of(e);
        }
    }

    @Override
    @Transactional
    public @NonNull Result<VideoStatsResponse> getVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            return updateOrInitSession(userId, messageId, paginationStateDto -> {
                var items = linkRepository.findFirstPage(videosPerPage);
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();

                var firstSeenId = items.isEmpty() ? null : items.getFirst().id();
                var lastSeenId = items.isEmpty() ? null : items.getLast().id();
                var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                    ZonedDateTime.now(ZoneOffset.UTC));
                paginationStateRepository.save(state);

                var responseItems = items.stream()
                    .map(LinkDto::toVideoStatsItem)
                    .toList();
                var hasMore = totalLinks > items.size();
                var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, false,
                    totalViews);
                return Success.of(resp);
            });
        } catch (Throwable e) {
            log.error(
                "UserSessionServiceImpl[getVideos] userId={} messageId={} unhandled exception",
                userId, messageId, e);
            return Failure.of(e);
        }
    }

    @Override
    @Transactional
    public @NonNull Result<VideoStatsResponse> getNextVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            return updateOrInitSession(userId, messageId, paginationState -> {
                var totalLinks = linkRepository.getTotalLinkCount();

                if (paginationState.lastSeenId() >= totalLinks) {
                    log.error(
                        "UserSessionServiceImpl[getNextVideos] userId={} messageId={} lastSeenId=${} pagination overflow",
                        userId, messageId, paginationState.lastSeenId());
                    return Failure.of(new PageIsOutsideOfBoundsException(
                        "the last page has already been reached"));
                }

                var items = linkRepository.findNextPage(paginationState.lastSeenId(),
                    videosPerPage);
                var totalViews = linkRepository.getTotalViewSum();

                var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
                var lastSeenId = items.isEmpty() ? videosPerPage : items.getLast().id();

                var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                    ZonedDateTime.now(ZoneOffset.UTC));
                paginationStateRepository.save(state);

                var responseItems = items.stream()
                    .map(LinkDto::toVideoStatsItem)
                    .toList();
                var hasMore = lastSeenId < totalLinks;
                var hasPrev = firstSeenId > 1;
                var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, hasPrev,
                    totalViews);
                return Success.of(resp);

            });
        } catch (Throwable e) {
            log.error(
                "UserSessionServiceImpl[getNextVideos] userId={} messageId={} unhandled exception",
                userId, messageId, e);
            return Failure.of(e);
        }
    }

    @Override
    @Transactional
    public @NonNull Result<VideoStatsResponse> getPreviousVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            return updateOrInitSession(userId, messageId, paginationState -> {
                if (paginationState.firstSeenId() <= 1) {
                    log.error(
                        "UserSessionServiceImpl[getPreviousVideos] userId={} messageId={} lastSeenId=${} pagination underflow",
                        userId, messageId, paginationState.lastSeenId());
                    return Failure.of(new PageIsOutsideOfBoundsException(
                        "attempted to access a page with negative number"));
                }

                var items = linkRepository.findPreviousPage(paginationState.firstSeenId(),
                    videosPerPage);
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();

                var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
                var lastSeenId = items.isEmpty() ? videosPerPage : items.getLast().id();

                var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                    ZonedDateTime.now(ZoneOffset.UTC));
                paginationStateRepository.save(state);

                var responseItems = items.stream()
                    .map(LinkDto::toVideoStatsItem)
                    .toList();
                var hasMore = lastSeenId < totalLinks;
                var hasPrev = firstSeenId > 1;
                var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, hasPrev,
                    totalViews);
                return Success.of(resp);
            });
        } catch (Throwable e) {
            log.error(
                "UserSessionServiceImpl[getPreviousVideos] userId={} messageId={} unhandled exception",
                userId, messageId, e);
            return Failure.of(e);
        }
    }

    private @NonNull Result<VideoStatsResponse> updateOrInitSession(Long userId, Long messageId,
        Function<PaginationStateDto, Result<VideoStatsResponse>> f) {
        var maybePaginationState = paginationStateRepository.find(userId, messageId);
        var paginationState = maybePaginationState.orElseGet(
            () -> new PaginationStateDto(userId, messageId, 0L, (long) videosPerPage,
                ZonedDateTime.now(
                    ZoneId.of("UTC"))));
        return f.apply(paginationState);
    }

    @Override
    public void refreshVideos(@NonNull Long userId, @NonNull Long messageId,
        Consumer<Result<RefreshVideosPagedResponse>> callback) {
        // TODO: переписать с использованием metadataByIds
        final Consumer<Result<RefreshVideosPagedResponse>> cb =
            callback != null ? callback : result -> {
            };

        if (!refreshSemaphore.tryAcquire()) {
            cb.accept(Failure.of(new RefreshInProgressException(
                "it is forbidden to run refreshVideos() simultaneously")));
            return;
        }

        var workerThread = Thread.ofVirtual().unstarted(() -> {
            var videos = txManager.execute(linkRepository::findAllForUpdate);
            var hasErrors = false;
            for (int i = 0; i < videos.size(); i++) {
                var video = videos.get(i);
                try {
                    final var rawLink = video.rawLink();
                    hasErrors = hasErrors || txManager.execute(() -> addVideo(rawLink)).isFailure();
                    if (i < videos.size() - 1) {
                        Thread.sleep(refreshDelayMs);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Throwable e) {
                    log.error(
                        "ServiceLayerImpl[refreshVideos] unhandled exception while updating video id={}",
                        video.rawLink(), e);
                    hasErrors = true;
                }
            }
            final var hasErrorsFinal = hasErrors;
            var response = txManager.execute(() -> {
                var maybePaginationState = paginationStateRepository.find(userId, messageId);
                var firstSeenId =
                    maybePaginationState.isPresent() ? maybePaginationState.get().firstSeenId()
                        : 0L;
                var lastSeenId =
                    maybePaginationState.isPresent() ? maybePaginationState.get().lastSeenId()
                        : videosPerPage;
                var items = linkRepository.findNextPage(firstSeenId - 1, videosPerPage);
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();
                var responseItems = items.stream()
                    .map(LinkDto::toVideoStatsItem)
                    .toList();
                var hasNext = lastSeenId < totalLinks;
                var hasPrev = firstSeenId > 1;
                return new RefreshVideosPagedResponse(responseItems, totalLinks,
                    hasNext, hasPrev, totalViews, hasErrorsFinal);
            });
            cb.accept(Success.of(response));
            refreshSemaphore.release();
        });
        workerThread.setUncaughtExceptionHandler((tr, e) -> {
            log.error(
                "ServiceLayerImpl[refreshVideos] userId={} messageId={} unhandled exception in worker thread",
                userId, messageId, e);
            cb.accept(Failure.of(e));
            refreshSemaphore.release();
        });
        workerThread.start();
    }
}
