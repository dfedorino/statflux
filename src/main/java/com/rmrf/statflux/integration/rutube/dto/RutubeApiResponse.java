package com.rmrf.statflux.integration.rutube.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO для ответа RuTube API: GET /api/video/{id}
 *
 * Обрабатываем id, title, hits (просмотры) как views;
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RutubeApiResponse(
    String id,
    String title,
    @JsonProperty("hits") Long views
) {
}
