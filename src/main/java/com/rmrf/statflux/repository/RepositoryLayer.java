package com.rmrf.statflux.repository;

import com.rmrf.statflux.domain.dto.VideoStatsItem;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public interface RepositoryLayer {

    boolean save(@NonNull String hostingName, @NonNull String rawLink, @NonNull String hostingId,
        @NonNull String name, Long views);

    List<VideoStatsItem> getVideos(Optional<String> skip, Optional<Integer> take);

    int getTotalVideosCount();

    long getTotalViewCount();
}
