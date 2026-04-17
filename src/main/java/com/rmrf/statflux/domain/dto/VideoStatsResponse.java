package com.rmrf.statflux.domain.dto;

import java.util.List;
import lombok.NonNull;

public record VideoStatsResponse(@NonNull List<VideoStatsItem> items, int totalVideos,
                                 boolean hasMore, long totalViews) {

}
