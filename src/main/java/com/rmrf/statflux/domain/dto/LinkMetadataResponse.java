package com.rmrf.statflux.domain.dto;

import lombok.NonNull;

/**
 * @param rawUrl     полный url видео
 * @param platformId идентификатор видео на хостинге
 * @param name       название видео
 * @param views      количество просмотров
 */
public record LinkMetadataResponse(@NonNull String rawUrl, @NonNull String platformId,
                                   @NonNull String name, Long views) {

}
