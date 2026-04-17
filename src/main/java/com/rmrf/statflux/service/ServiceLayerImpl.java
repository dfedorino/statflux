package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.ParseVideoResponse;
import com.rmrf.statflux.domain.exceptions.UnsupportedUrlException;
import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.repository.RepositoryLayer;
import com.rmrf.statflux.util.result.Failure;
import com.rmrf.statflux.util.result.Result;
import com.rmrf.statflux.util.result.Success;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceLayerImpl implements ServiceLayer {

    private final RepositoryLayer repositoryLayer;

    public ServiceLayerImpl(RepositoryLayer repositoryLayer) {
        this.repositoryLayer = repositoryLayer;
    }

    @Override
    public @NonNull Result<AddVideoResponse> addVideo(@NonNull String rawUrl) {
        var hostingApiOpt = HostingApiFactory.forUrl(rawUrl);
        if (hostingApiOpt.isEmpty()) {
            log.error("ServiceLayerImpl[addVideo] no hosting api found for rawUrl={}", rawUrl);
            return Failure.of(new UnsupportedUrlException(rawUrl));
        }
        var hostingApi = hostingApiOpt.get();
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
                log.error("ServiceLayerImpl[addVideo] failed to parse rawUrl={} reason={}", rawUrl,
                    f.exception().getMessage());
                yield f.swap();
            }
        };
    }
}
