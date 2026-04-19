package com.rmrf.statflux.domain.dto;

import lombok.NonNull;

/**
 * @param id     идентификатор видео на хостинге
 * @param title  название видео
 * @param views  количество просмотров
 */
public record VideoMetadataResponse(@NonNull String id,
                                    @NonNull String title, Long views) {

}
