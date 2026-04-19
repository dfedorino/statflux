package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.LinkMetadataResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosResponse;
import com.rmrf.statflux.domain.dto.VideoStatsItem;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.exceptions.InternalTechErrorException;
import com.rmrf.statflux.domain.exceptions.RefreshInProgressException;
import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.repository.dto.LinkDto;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ServiceLayerImpl implements ServiceLayer {

    private final LinkRepository repositoryLayer;
    private final HostingApiFactory hostingApiFactory;
    private final long refreshDelayMs;

    private final Semaphore refreshSemaphore;

    public ServiceLayerImpl(LinkRepository linkRepository, HostingApiFactory hostingApiFactory,
        long refreshDelayMs) {
        this.repositoryLayer = linkRepository;
        this.hostingApiFactory = hostingApiFactory;
        if (refreshDelayMs <= 0) {
            throw new IllegalArgumentException("refreshDelayMs must be positive");
        }
        this.refreshDelayMs = refreshDelayMs;
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
                        ZonedDateTime.now()
                    );
                    var success = repositoryLayer.save(dbItem);
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
    public @NonNull Result<VideoStatsResponse> getVideos(Optional<Integer> skip,
        Optional<Integer> take) {
        try {
            var items = repositoryLayer.findAll();
            var totalLinks = repositoryLayer.getTotalLinkCount();
            var totalViews = repositoryLayer.getTotalViewSum();
            var responseItems = items.stream()
                .map(l -> new VideoStatsItem(l.rawLink(), l.title(), l.rawLink(), l.views(),
                    l.updatedAt()))
                .toList();
            var hasMore = totalLinks > items.size();
            var hasPrev = skip.filter(s -> s > 0).isPresent();
            var resp = new VideoStatsResponse(responseItems, totalLinks, hasMore, hasPrev,
                totalViews);
            return Success.of(resp);
        } catch (Throwable e) {
            log.error("ServiceLayerImpl[getVideos] unhandled exception", e);
            return Failure.of(e);
        }
    }

    @Override
    public void refreshVideos(Consumer<Result<RefreshVideosResponse>> callback) {
        // TODO: переписать с использованием metadataByIds
        final Consumer<Result<RefreshVideosResponse>> cb = callback != null ? callback : result -> {
        };

        if (!refreshSemaphore.tryAcquire()) {
            cb.accept(Failure.of(new RefreshInProgressException(
                "it is forbidden to run refreshVideos() simultaneously")));
            return;
        }

        var workerThread = Thread.ofVirtual().unstarted(() -> {
            var videos = repositoryLayer.findAll();
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
            cb.accept(Success.of(new RefreshVideosResponse(hasErrors)));
            refreshSemaphore.release();
        });
        workerThread.setUncaughtExceptionHandler((tr, e) -> {
            log.error("ServiceLayerImpl[refreshVideos] unhandled exception in worker thread", e);
            cb.accept(Failure.of(e));
            refreshSemaphore.release();
        });
        workerThread.start();
    }
}
