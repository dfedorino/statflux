package com.rmrf.statflux.domain.dto;

import lombok.NonNull;

/**
 * @param hostingName иденитификатор хостинга
 * @param name        название видео
 * @param rawUrl      полный url видео
 * @param views       количество просмотров
 */
public record AddVideoResponse(@NonNull String hostingName, @NonNull String name,
                               @NonNull String rawUrl,
                               @NonNull Long views) {

}
