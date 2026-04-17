package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.result.Result;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.NonNull;

public interface ServiceLayer {


    /**
     * @param rawUrl url видео
     * @return результат добавления видео для последующего отслеживания
     */
    @NonNull
    Result<AddVideoResponse> addVideo(@NonNull String rawUrl);

    /**
     * @param skip пагинация - сколько документов пропустить
     * @param take пагинация - сколько документов вернуть на одной странице
     */
    @NonNull
    Result<VideoStatsResponse> getVideos(Optional<String> skip, Optional<Integer> take);

    @NonNull
    default Result<VideoStatsResponse> getVideos() {
        return getVideos(Optional.empty(), Optional.empty());
    }

    /**
     * Запуск асинхронного процесса обновления метаданных видео. Результат обновления возвращается
     * через механизм коллбэка. Одновременный запуск нескольких процессов обновления запрещён.
     *
     * @param callback вызывается по окончании процесса обновления данных видео. Может вернуть
     *                 Success - процесс обновления завершился успешно, но единичные видео могли
     *                 быть не обработаны из-за ошибок. Failure - процесс обновления полностью
     *                 завершился неуспехом
     */
    void refreshVideos(Consumer<Result<RefreshVideosResponse>> callback);
}
