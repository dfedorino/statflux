package com.rmrf.statflux.domain.dto;

import java.util.List;
import lombok.NonNull;

public class VideoStatsResponse {
    @NonNull
    private final List<VideoStatsItem> items;
    private final int totalVideos;
    private final boolean hasNext;
    private final boolean hasPrev;
    private final long totalViews;

    public VideoStatsResponse(@NonNull List<VideoStatsItem> items, int totalVideos,
                              boolean hasNext, boolean hasPrev, long totalViews) {
        this.items = items;
        this.totalVideos = totalVideos;
        this.hasNext = hasNext;
        this.hasPrev = hasPrev;
        this.totalViews = totalViews;
    }

    public @NonNull List<VideoStatsItem> getItems() {
        return items;
    }

    public int getTotalVideos() {
        return totalVideos;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public boolean hasPrev() {
        return hasPrev;
    }

    public long getTotalViews() {
        return totalViews;
    }
}
