package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.util.result.Result;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.NonNull;

public interface ServiceLayer {

    @NonNull
    Result<AddVideoResponse> addVideo(@NonNull String rawUrl);

    @NonNull
    Result<VideoStatsResponse> getVideos(Optional<String> skip, Optional<Integer> take);

    @NonNull
    default Result<VideoStatsResponse> getVideos() {
        return getVideos(Optional.empty(), Optional.empty());
    }

    void refreshVideos(Consumer<List<Result<AddVideoResponse>>> callback);
}
