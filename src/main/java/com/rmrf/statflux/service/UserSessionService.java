package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.result.Result;
import java.util.function.Consumer;
import lombok.NonNull;

public interface UserSessionService {

    /**
     * @param rawUrl url видео
     * @return результат добавления видео для последующего отслеживания
     */
    @NonNull
    Result<AddVideoResponse> addVideo(@NonNull String rawUrl);

    @NonNull
    Result<VideoStatsResponse> getVideos(@NonNull Long userId, @NonNull Long messageId);

    @NonNull
    Result<VideoStatsResponse> getNextVideos(@NonNull Long userId, @NonNull Long messageId);

    @NonNull
    Result<VideoStatsResponse> getPreviousVideos(@NonNull Long userId, @NonNull Long messageId);

    /**
     * Запуск асинхронного процесса обновления метаданных видео. Результат обновления возвращается
     * через механизм коллбэка. Одновременный запуск нескольких процессов обновления запрещён.
     *
     * @param callback вызывается по окончании процесса обновления данных видео. Может вернуть
     *                 Success - процесс обновления завершился успешно, но единичные видео могли
     *                 быть не обработаны из-за ошибок. Failure - процесс обновления полностью
     *                 завершился неуспехом
     */
    void refreshVideos(@NonNull Long userId, @NonNull Long messageId,
        Consumer<Result<RefreshVideosPagedResponse>> callback);

}
