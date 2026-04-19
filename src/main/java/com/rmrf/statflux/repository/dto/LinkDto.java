package com.rmrf.statflux.repository.dto;

import java.time.ZonedDateTime;

/**
 * @param hostingName идентификатор хостинга
 * @param rawLink     необработанный url видео
 * @param hostingId   идентификатор видео по данным хостинга
 * @param title       заголовок видео
 * @param views       количество просмотров
 * @param updatedAt   время последнего обновления
 */
public record LinkDto(
    Long id,
    String hostingName,
    String rawLink,
    String hostingId,
    String title,
    long views,
    ZonedDateTime updatedAt
) {

}
