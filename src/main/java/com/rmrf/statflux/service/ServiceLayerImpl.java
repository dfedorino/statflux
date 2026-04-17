package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.ParseVideoResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.repository.RepositoryLayer;
import com.rmrf.statflux.util.result.Failure;
import com.rmrf.statflux.util.result.Result;
import com.rmrf.statflux.util.result.Success;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceLayerImpl implements ServiceLayer {

    private final RepositoryLayer repositoryLayer;
    private final HostingApiFactory hostingApiFactory;

    public ServiceLayerImpl(RepositoryLayer repositoryLayer, HostingApiFactory hostingApiFactory) {
        this.repositoryLayer = repositoryLayer;
        this.hostingApiFactory = hostingApiFactory;
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
            return switch (hostingApi.viewCount(rawUrl)) {
                case Success<ParseVideoResponse> s -> {
                    var success = repositoryLayer.save(
                        hostingApi.hostingName(),
                        rawUrl,
                        s.result().platformId(),
                        s.result().name(),
                        s.result().views());
                    if (!success) {
                        log.error("ServiceLayerImpl[addVideo] failed to save rawUrl={}", rawUrl);
                        yield Failure.of(new Exception("todo"));
                    }
                    yield Success.of(new AddVideoResponse(hostingApi.hostingName()));
                }
                case Failure<ParseVideoResponse> f -> {
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
    public @NonNull Result<VideoStatsResponse> getVideos(Optional<String> skip,
        Optional<Integer> take) {
        try {
            var items = repositoryLayer.getVideos(skip, take);
            var totalLinks = repositoryLayer.getTotalVideosCount();
            var totalViews = repositoryLayer.getTotalViewCount();
            var hasMore = totalLinks > items.size();
            var resp = new VideoStatsResponse(items, totalLinks, hasMore, totalViews);
            return Success.of(resp);
        } catch (Throwable e) {
            log.error("ServiceLayerImpl[getVideos] unhandled exception", e);
            return Failure.of(e);
        }
    }

    @Override
    public void refreshVideos(Consumer<List<Result<AddVideoResponse>>> callback) {

    }
}
