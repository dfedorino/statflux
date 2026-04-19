package com.rmrf.statflux.integration.youtube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO для ответа YouTube API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubeApiResponse(List<VideoItem> items) {

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VideoItem(String id, Statistics statistics, Snippet snippet) {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Statistics(@JsonProperty("viewCount") String viewCount) {

        public Long getViewCountAsLong() {
            if (viewCount == null || viewCount.isBlank()) {
                return 0L;
            }
            try {
                return Long.parseLong(viewCount);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(String title) {

    }
}
