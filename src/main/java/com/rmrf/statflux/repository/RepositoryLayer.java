package com.rmrf.statflux.repository;

import com.rmrf.statflux.domain.dto.VideoStatsItem;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public interface RepositoryLayer {

    boolean save(@NonNull String hostingName, @NonNull String rawLink, @NonNull String hostingId,
        @NonNull String name, Long views);

    @NonNull
    List<VideoStatsItem> getVideos(Optional<String> skip, Optional<Integer> take);

    @NonNull
    default List<VideoStatsItem> getVideos() {
        return getVideos(Optional.empty(), Optional.empty());
    }

    int getTotalVideosCount();

    long getTotalViewCount();
}
