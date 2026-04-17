package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.util.result.Result;
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

    /**
     * @param callback вызывается по окончании процесса обновления данных видео. Может вернуть
     *                 Success - процесс обновления завершился успешно, но единичные видео могли
     *                 быть не обработаны. Failure - процесс обновления полностью завершился
     *                 неуспехом
     */
    void refreshVideos(Consumer<Result<RefreshVideosResponse>> callback);
}
