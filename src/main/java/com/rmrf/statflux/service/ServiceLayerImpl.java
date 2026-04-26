package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.constant.Platform;
import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.exceptions.InternalTechErrorException;
import com.rmrf.statflux.domain.exceptions.LinkIdNotFoundException;
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
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    public @NonNull Result<AddVideoResponse> addVideo(@NonNull Long userId, @NonNull String rawUrl) {
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
                        userId,
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
            return initSession(userId, messageId, paginationStateDto -> {
                var items = linkRepository.findFirstPage(userId, videosPerPage);
                var totalLinks = linkRepository.getTotalLinkCount(userId);
                var totalViews = linkRepository.getTotalViewSum(userId);

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
            return updateCurrentOrPrevSession(userId, messageId, paginationState -> {
                var maxId = linkRepository.findMaxId(userId);

                if (maxId.isPresent() && paginationState.lastSeenId() >= maxId.get()) {
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
                var hasMore = maxId.isPresent() && lastSeenId < maxId.get();
                var minId = linkRepository.findMinId(userId);
                var hasPrev = minId.isPresent() && firstSeenId > minId.get();
                var totalLinks = linkRepository.getTotalLinkCount(userId);
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
            return updateCurrentOrPrevSession(userId, messageId, paginationState -> {
                var minId = linkRepository.findMinId(userId);
                if (minId.isPresent() && paginationState.firstSeenId() <= minId.get()) {
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
                var maxId = linkRepository.findMaxId(userId);
                var hasMore = maxId.isPresent() && lastSeenId < maxId.get();
                var hasPrev = minId.isPresent() && firstSeenId > minId.get();
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

    private @NonNull Result<VideoStatsResponse> initSession(Long userId, Long messageId,
        Function<PaginationStateDto, Result<VideoStatsResponse>> f) {
        var paginationState = new PaginationStateDto(userId, messageId, 0L, (long) videosPerPage,
                ZonedDateTime.now(ZoneId.of("UTC")));
        return f.apply(paginationState);
    }

    private @NonNull Result<VideoStatsResponse> updateCurrentOrPrevSession(Long userId, Long messageId,
        Function<PaginationStateDto, Result<VideoStatsResponse>> f) {
        return f.apply(paginationStateRepository.find(userId, messageId)
            .or(() -> paginationStateRepository.find(userId, messageId - 1))
            // should never end up here...
            .orElseGet(() -> new PaginationStateDto(userId, messageId, 0L,
                (long) videosPerPage,
                ZonedDateTime.now(ZoneId.of("UTC")))));
    }

    @Override
    public void refreshVideos(@NonNull Long userId, @NonNull Long messageId,
        Consumer<Result<RefreshVideosPagedResponse>> callback) {
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
            var videosByHosting = videos.stream()
                .collect(Collectors.groupingBy(LinkDto::hostingName));

            var hasErrors = new AtomicBoolean(false);
            videosByHosting.forEach((hosting, vs) -> {
                var platformName = vs.getFirst().hostingName();
                try {
                    var platform = Platform.valueOf(platformName.toUpperCase());
                    var hostingApiEither = hostingApiFactory.forPlatform(platform);
                    if (hostingApiEither.isFailure()) {
                        log.error(
                            "ServiceLayerImpl[refreshVideos] couldn't resolve hostingApi for {}",
                            platformName);
                        return;
                    }
                    var hostingApi = hostingApiEither.get();
                    var videosByHostingIds = vs.stream().collect(Collectors.toMap(
                        LinkDto::hostingId,
                        k -> k
                    ));
                    var hostingIds = videosByHostingIds.keySet().stream().toList();
                    switch (hostingApi.metadataByIds(hostingIds)) {
                        case Success<List<VideoMetadataResponse>> s -> {
                            for (var updateMetadata : s.result()) {
                                videosByHostingIds.compute(updateMetadata.id(), (k, v) -> {
                                    if (v == null) {
                                        return new LinkDto(null, userId, hostingApi.hostingName(),
                                            "undefined",
                                            updateMetadata.id(), updateMetadata.title(),
                                            updateMetadata.views(), ZonedDateTime.now(
                                            ZoneId.of("UTC")));
                                    } else {
                                        return new LinkDto(v.id(), userId, v.hostingName(), v.rawLink(),
                                            v.hostingId(), updateMetadata.title(),
                                            updateMetadata.views(), ZonedDateTime.now(
                                            ZoneId.of("UTC")));
                                    }
                                });
                            }

                            txManager.execute(() -> {
                                videosByHostingIds.values().forEach(linkRepository::save);
                                return null;
                            });
                        }
                        default -> hasErrors.set(true);
                    }
                } catch (Exception e) {
                    log.error("ServiceLayerImpl[refreshVideos] unhandled exception", e);
                    hasErrors.set(true);
                }
            });

            final var hasErrorsFinal = hasErrors.get();
            var response = txManager.execute(() -> {
                var maybePaginationState = paginationStateRepository.find(userId, messageId)
                    .or(() -> paginationStateRepository.find(userId, messageId - 1));
                var firstSeenId =
                    maybePaginationState.isPresent() ? maybePaginationState.get().firstSeenId()
                        : 1L;
                var lastSeenId =
                    maybePaginationState.isPresent() ? maybePaginationState.get().lastSeenId()
                        : videosPerPage;
                var items = linkRepository.findBetweenIds(firstSeenId, lastSeenId);
                if (maybePaginationState.isEmpty()) {
                    var realLastSeenId = items.isEmpty() ? lastSeenId : items.getLast().id();
                    var paginationState = new PaginationStateDto(userId, messageId, firstSeenId,
                        realLastSeenId, ZonedDateTime.now(ZoneId.of("UTC")));
                    paginationStateRepository.save(paginationState);
                }
                var totalLinks = linkRepository.getTotalLinkCount();
                var totalViews = linkRepository.getTotalViewSum();
                var responseItems = items.stream()
                    .map(LinkDto::toVideoStatsItem)
                    .toList();
                var hasNext = lastSeenId < totalLinks;
                var minId = linkRepository.findMinId(userId);
                var hasPrev = minId.isPresent() && firstSeenId > minId.get();
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

    @Override
    @Transactional
    public @NonNull Result<Boolean> deleteVideo(@NonNull Long userId, long linkId) {
        try {
            var deleted = linkRepository.delete(userId, linkId);
            if (!deleted) {
                log.error("ServiceLayerImpl[deleteVideo] no link found for userId={} linkId={}", userId, linkId);
                return Failure.of(new LinkIdNotFoundException("no link found for id=" + linkId));
            }
            return Success.of(true);
        } catch (Exception e) {
            log.error("ServiceLayerImpl[deleteVideo] unhandled exception", e);
            return Failure.of(e);
        }
    }
}
