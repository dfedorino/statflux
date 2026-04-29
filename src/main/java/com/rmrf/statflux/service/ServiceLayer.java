package com.rmrf.statflux.service;

import com.rmrf.statflux.domain.dto.AddVideoResponse;
import com.rmrf.statflux.domain.dto.RefreshVideosPagedResponse;
import com.rmrf.statflux.domain.dto.VideoStatsResponse;
import com.rmrf.statflux.domain.result.Result;
import java.util.function.Consumer;
import lombok.NonNull;

public interface ServiceLayer {


    /**
     * Добавление нового видео для конкретного пользователя/чата
     *
     * @param userId    идентификатор текущего пользователя/чата
     * @param rawUrl url видео
     * @return результат добавления видео для последующего отслеживания
     */
    @NonNull
    Result<AddVideoResponse> addVideo(@NonNull Long userId, @NonNull String rawUrl);

    /**
     * Получение списка видео и статистики для первой страницы / сброс страницы на первую
     *
     * @param userId    идентификатор текущего пользователя/чата
     * @param messageId идентификатор текущего сообщения
     * @return {@link VideoStatsResponse}
     */
    @NonNull
    Result<VideoStatsResponse> getVideos(@NonNull Long userId, @NonNull Long messageId);

    /**
     * Получение списка видео и статистики для следующей страницы / переход на следующую страницу
     *
     * @param userId    идентификатор текущего пользователя/чата
     * @param messageId идентификатор текущего сообщения
     * @return {@link VideoStatsResponse}
     */
    @NonNull
    Result<VideoStatsResponse> getNextVideos(@NonNull Long userId, @NonNull Long messageId);

    /**
     * Получение списка видео и статистики для следующей страницы / переход на следующую страницу
     *
     * @param userId    идентификатор текущего пользователя/чата
     * @param messageId идентификатор текущего сообщения
     * @return {@link VideoStatsResponse}
     */
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


    /**
     * Удаление видео по ID для конкретного пользователя/чата
     *
     * @param userId идентификатор текущего пользователя/чата
     * @param linkId идентификатор ссылки
     * @return результат удаления ссылки из отслеживаемых
     */
    @NonNull
    Result<Boolean> deleteVideo(@NonNull Long userId, long linkId);
}
