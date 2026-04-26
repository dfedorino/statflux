package com.rmrf.statflux.domain.dto;

import lombok.NonNull;

/**
 * @param id          иденитификатор видео в сервисе
 * @param hostingName иденитификатор хостинга
 * @param title       название видео
 * @param rawUrl      полный url видео
 * @param views       количество просмотров
 */
public record AddVideoResponse(@NonNull String id,
                               @NonNull String hostingName,
                               @NonNull String title,
                               @NonNull String rawUrl,
                               @NonNull Long views) {

}
