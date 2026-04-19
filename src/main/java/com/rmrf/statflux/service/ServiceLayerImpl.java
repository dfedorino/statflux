package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.LinkMetadataResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoStatsItem;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.exceptions.InternalTechErrorException;
import com.rmrf.statflux.domain.exceptions.RefreshInProgressException;
import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.dto.LinkDto;
import com.rmrf.statflux.repository.dto.PaginationStateDto;
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
    private final HostingApiFactory hostingApiFactory;
    private final long refreshDelayMs;
    private final int videosPerPage;

    private final Semaphore refreshSemaphore;

    public ServiceLayerImpl(LinkRepository linkRepository,
        PaginationStateRepository paginationStateRepository, HostingApiFactory hostingApiFactory,
        long refreshDelayMs, int videosPerPage) {
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
        refreshSemaphore = new Semaphore(1);
    }

    @Override
    public @NonNull Result<AddVideoResponse> addVideo(@NonNull String rawUrl) {
        try {
            var hostingApiEither = hostingApiFactory.forUrl(rawUrl);
            if (hostingApiEither.isFailure()) {
                log.error("ServiceLayerImpl[addVideo] no hosting api found for rawUrl={}", rawUrl);
                return hostingApiEither.asFailure().swap();
            }
            var hostingApi = hostingApiEither.get();
            return switch (hostingApi.metadataByLink(rawUrl)) {
                case Success<LinkMetadataResponse> s -> {
                    var dbItem = new LinkDto(
                        null,
                        hostingApi.hostingName(),
                        s.result().rawUrl(),
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
                            s.result().rawUrl(),
                            s.result().views()));
                }
                case Failure<LinkMetadataResponse> f -> {
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
    public @NonNull Result<VideoStatsResponse> getVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            var items = linkRepository.findFirstPage(videosPerPage);
            var totalLinks = linkRepository.getTotalLinkCount();
            var totalViews = linkRepository.getTotalViewSum();

            var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
            var lastSeenId = items.isEmpty() ? 0 : items.getLast().id();
            var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                ZonedDateTime.now(ZoneOffset.UTC));
            paginationStateRepository.save(state);

            var responseItems = items.stream()
                .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(), l.views(),
                    l.updatedAt()))
                .toList();
            var hasMore = totalLinks > items.size();
            var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, false,
                totalViews);
            return Success.of(resp);
        } catch (Throwable e) {
            log.error(
                "UserSessionServiceImpl[getVideos] userId={} messageId={} unhandled exception",
                userId, messageId, e);
            return Failure.of(e);
        }
    }

    @Override
    public @NonNull Result<VideoStatsResponse> getNextVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            return updateOrInitSession(userId, messageId, paginationState -> {
                var items = linkRepository.findNextPage(paginationState.lastSeenId(),
                    videosPerPage);
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();

                var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
                var lastSeenId = items.isEmpty() ? 0 : items.getLast().id();

                var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                    ZonedDateTime.now(ZoneOffset.UTC));
                paginationStateRepository.save(state);

                var responseItems = items.stream()
                    .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(), l.views(),
                        l.updatedAt()))
                    .toList();
                var hasMore = totalLinks > items.size();
                var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, true,
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
    public @NonNull Result<VideoStatsResponse> getPreviousVideos(@NonNull Long userId,
        @NonNull Long messageId) {
        try {
            return updateOrInitSession(userId, messageId, paginationState -> {
                var items = linkRepository.findPreviousPage(paginationState.firstSeenId(),
                    videosPerPage);
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();

                var firstSeenId = items.isEmpty() ? 0 : items.getFirst().id();
                var lastSeenId = items.isEmpty() ? 0 : items.getLast().id();

                var state = new PaginationStateDto(userId, messageId, firstSeenId, lastSeenId,
                    ZonedDateTime.now(ZoneOffset.UTC));
                paginationStateRepository.save(state);

                var responseItems = items.stream()
                    .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(), l.views(),
                        l.updatedAt()))
                    .toList();
                var hasMore = totalLinks > items.size();
                var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, false,
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
        if (maybePaginationState.isEmpty()) {
            log.info(
                "UserSessionServiceImpl[updateOrInitSession] userId={} messageId={} session not found",
                userId, messageId);
            return getVideos(userId, messageId);
        }
        var paginationState = maybePaginationState.get();
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
            var videos = linkRepository.findAll();
            var hasErrors = false;
            for (int i = 0; i < videos.size(); i++) {
                var video = videos.get(i);
                try {
                    hasErrors = hasErrors || addVideo(video.rawLink()).isFailure();
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
            var paginationState = paginationStateRepository.find(userId, messageId);
            var firstSeenId =
                paginationState.isPresent() ? paginationState.get().firstSeenId() : 0L;
            var items = linkRepository.findNextPage(firstSeenId, videosPerPage);
            var totalVideos = linkRepository.getTotalLinkCount();
            var totalViews = linkRepository.getTotalViewSum();
            var responseItems = items.stream()
                .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(),
                    l.views(),
                    l.updatedAt()))
                .toList();
            var hasNext = responseItems.size() < totalVideos;
            var hasPrev = firstSeenId > 0;
            var response = new RefreshVideosPagedResponse(responseItems, totalVideos,
                hasNext,
                hasPrev, totalViews, hasErrors);
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
