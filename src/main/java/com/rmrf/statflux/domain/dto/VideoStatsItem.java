package com.rmrf.statflux.domain.dto;

import java.time.ZonedDateTime;
import lombok.NonNull;

/**
 * @param id        идентификатор видео в сервисе
 * @param name      название видео
 * @param rawUrl    полный url видео
 * @param views     количество просмотров
 * @param updatedAt время последнего обновления
 */
public record VideoStatsItem(@NonNull String id, @NonNull String name, @NonNull String rawUrl,
                             @NonNull Long views, @NonNull ZonedDateTime updatedAt) {

}
