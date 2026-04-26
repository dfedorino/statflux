package com.rmrf.statflux.repository.dto;

import com.rmrf.statflux.domain.dto.VideoStatsItem;
import java.time.ZonedDateTime;
import lombok.NonNull;

/**
 * @param chatId      идентификатор чата пользователя
 * @param hostingName идентификатор хостинга
 * @param rawLink     необработанный url видео
 * @param hostingId   идентификатор видео по данным хостинга
 * @param title       заголовок видео
 * @param views       количество просмотров
 * @param updatedAt   время последнего обновления
 */
public record LinkDto(
    Long id,
    Long chatId,
    String hostingName,
    String rawLink,
    String hostingId,
    String title,
    long views,
    ZonedDateTime updatedAt
) {

    public @NonNull VideoStatsItem toVideoStatsItem() {
        return new VideoStatsItem(id().toString(), title(), rawLink(), views(), updatedAt());
    }
}
