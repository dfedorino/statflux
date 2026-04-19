package com.rmrf.statflux.integration.youtube;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmrf.statflux.domain.constant.Platform;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.exceptions.BadUrlException;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.youtube.dto.YouTubeApiResponse;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Реализация YouTubeHostingApi. Отвечает за получение данных видео с YouTube API
 */
@Slf4j
public class YouTubeHostingApiImpl implements YouTubeHostingApi {

    private static final String API_BASE_URL = "https://www.googleapis.com/youtube/v3/videos";
    private final String apiKey;
    private final SimpleHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public YouTubeHostingApiImpl(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new SimpleHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public YouTubeHostingApiImpl(String apiKey, SimpleHttpClient httpClient) {
        this.apiKey = apiKey;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Result<VideoMetadataResponse> metadataByLink(String rawLink) {
        return metadataById(rawLink);
    }

    /**
     * Получает данные видео по ссылке
     */
    @Override
    public @NonNull Result<VideoMetadataResponse> metadataById(String id) {
        try {
            Result<List<VideoMetadataResponse>> result = metadataByIds(List.of(id));

            if (result.isFailure()) {
                return Failure.of(((Failure<List<VideoMetadataResponse>>) result).exception());
            }

            List<VideoMetadataResponse> results = result.get();

            if (results.isEmpty()) {
                return Failure.of(new RuntimeException("Video not found: " + id));
            }

            return Success.of(results.getFirst());
        } catch (BadUrlException e) {
            log.error("Invalid YouTube link format: {}", id, e);
            return Failure.of(
                new RuntimeException("Invalid YouTube link format: " + e.getMessage()));
        }
    }

    /**
     * Получает данные видео по списку id
     */
    @Override
    public @NonNull Result<List<VideoMetadataResponse>> metadataByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("ids list is empty!");
            return Failure.of(new Exception("ids list is empty!"));
        }

        String idsParam = String.join(",", ids);
        String url = String.format("%s?part=statistics,snippet&id=%s&key=%s", API_BASE_URL,
            idsParam, apiKey);

        try {
            String responseBody = httpClient.get(url);
            YouTubeApiResponse response = objectMapper.readValue(responseBody,
                YouTubeApiResponse.class);

            if (!response.hasItems()) {
                log.warn("No videos found for ids: {}", ids);
                return Success.of(List.of());
            }

            return Success.of(
                response.items().stream().filter(item -> item != null && item.snippet() != null)
                    .map(this::buildMetadataFromItem).toList());
        } catch (Exception e) {
            log.error("Failed to fetch videos for ids: {}", ids, e);
            return Failure.of(new Exception(e.getMessage()));
        }
    }

    /**
     * Конвертирует данные из ответа Youtube в VideoMetadataResponse
     */
    private VideoMetadataResponse buildMetadataFromItem(YouTubeApiResponse.VideoItem videoItem) {
        Long viewsCount =
            videoItem.statistics() != null ? videoItem.statistics().getViewCountAsLong() : 0L;

        return new VideoMetadataResponse(videoItem.id(), videoItem.snippet().title(), viewsCount);
    }

    @Override
    public @NonNull String hostingName() {
        return Platform.YOUTUBE.getDisplayName();
    }
}
