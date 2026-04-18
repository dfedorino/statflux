package com.rmrf.statflux.domain.dto;

import java.util.List;
import lombok.NonNull;

/**
 * @param items       список видео
 * @param totalVideos общее количество всех видео в базе
 * @param hasNext     есть ли следующая страница
 * @param totalViews  общее количество просмотром всех видео в базе
 */
public record VideoStatsResponse(@NonNull List<VideoStatsItem> items, int totalVideos,
                                 boolean hasNext, long totalViews) {

}
