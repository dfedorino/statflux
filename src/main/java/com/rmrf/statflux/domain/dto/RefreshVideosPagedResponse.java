package com.rmrf.statflux.domain.dto;

import java.util.List;
import lombok.NonNull;

public class RefreshVideosPagedResponse extends VideoStatsResponse {
    private final boolean hasErrors;

    public RefreshVideosPagedResponse(@NonNull List<VideoStatsItem> items, int totalVideos,
                                      boolean hasNext, boolean hasPrev, long totalViews,
                                      boolean hasErrors) {
        super(items, totalVideos, hasNext, hasPrev, totalViews);
        this.hasErrors = hasErrors;
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}
