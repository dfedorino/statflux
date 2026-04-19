package com.rmrf.statflux.domain.dto;

import lombok.NonNull;

/**
 * @param rawUrl полный url видео
 * @param id     идентификатор видео на хостинге
 * @param title  название видео
 * @param views  количество просмотров
 */
public record LinkMetadataResponse(@NonNull String rawUrl, @NonNull String id,
                                   @NonNull String title, Long views) {

}
