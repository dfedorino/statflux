package com.rmrf.statflux.integration.rutube;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmrf.statflux.domain.constant.Platform;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.exceptions.BadUrlException;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.rutube.dto.RutubeApiResponse;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация RutubeVideoProvider.
 * Endpoint: GET {apiBaseUrl}/{videoId}, без авторизации
 */
@Slf4j
public class RutubeVideoProviderImpl implements RutubeVideoProvider {

    private static final String USER_AGENT = "statflux/1.0";

    private final String apiBaseUrl;
    private final SimpleHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RutubeVideoProviderImpl(String apiBaseUrl, SimpleHttpClient httpClient) {
        this.apiBaseUrl = apiBaseUrl;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Deprecated
    @Override
    public @NonNull Result<VideoMetadataResponse> metadataByLink(String rawLink) {
        return metadataById(rawLink);
    }

    @Override
    public @NonNull Result<VideoMetadataResponse> metadataById(String idOrUrl) {
        String videoId;
        try {
            videoId = idOrUrl != null && idOrUrl.contains("://")
                ? RutubeUrlParser.extractVideoId(idOrUrl)
                : idOrUrl;
        } catch (BadUrlException e) {
            log.error("Invalid RuTube link format: {}", idOrUrl, e);
            return Failure.of(new RuntimeException("Invalid RuTube link: " + e.getMessage()));
        }

        try {
            String body = httpClient.get(apiBaseUrl + "/" + videoId, Map.of("User-Agent", USER_AGENT));
            RutubeApiResponse dto = objectMapper.readValue(body, RutubeApiResponse.class);
            if (dto.id() == null || dto.title() == null) {
                log.warn("RuTube returned incomplete data for id={}", videoId);
                return Failure.of(new RuntimeException("Incomplete data for id=" + videoId));
            }
            long views = dto.views() != null ? dto.views() : 0L;
            return Success.of(new VideoMetadataResponse(dto.id(), dto.title(), views));
        } catch (Exception e) {
            log.error("Failed to fetch RuTube video id={}: {}", videoId, e.getMessage());
            return Failure.of(e);
        }
    }

    // TODO: sequential. Переписать через CompletableFuture.supplyAsync на VirtualThreadExecutor?
    // когда refresh 50+ ссылок станет узким местом
    @Override
    public @NonNull Result<List<VideoMetadataResponse>> metadataByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Failure.of(new Exception("ids list is empty!"));
        }
        List<VideoMetadataResponse> results = new ArrayList<>(ids.size());
        for (String id : ids) {
            Result<VideoMetadataResponse> result = metadataById(id);
            if (result.isSuccess()) {
                results.add(result.get());
            } else {
                log.warn("Skipping RuTube id={}: {}", id, result.asFailure().exception().getMessage());
            }
        }
        if (results.isEmpty()) {
            return Failure.of(new Exception("Failed to fetch any RuTube videos from ids=" + ids));
        }
        return Success.of(results);
    }

    @Override
    public @NonNull String hostingName() {
        return Platform.RUTUBE.getDisplayName();
    }
}
