package com.rmrf.statflux.repository;

import com.rmrf.statflux.domain.dto.VideoStatsItem;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

public interface RepositoryLayer {

    /**
     * Сохранение (upsert) метаданных видео
     *
     * @param hostingName идентификатор хостинга
     * @param rawLink     необработанный url видео
     * @param hostingId   идентификатор видео по данным хостинга
     * @param name        заголовок видео
     * @param views       количество просмотров
     * @return true - операция успешна, false - нет
     */
    boolean save(@NonNull String hostingName, @NonNull String rawLink, @NonNull String hostingId,
        @NonNull String name, Long views);

    /**
     * @param skip пагинация - сколько документов пропустить
     * @param take пагинация - сколько документов вернуть на одной странице
     */
    @NonNull
    List<VideoStatsItem> getVideos(Optional<Integer> skip, Optional<Integer> take);

    /**
     * @return данные всех сохранённых видео
     */
    @NonNull
    default List<VideoStatsItem> getVideos() {
        return getVideos(Optional.empty(), Optional.empty());
    }

    /**
     * @return суммарное количество видео в базе
     */
    int getTotalVideosCount();

    /**
     * @return суммарное количество просмотров всех видео в базе
     */
    long getTotalViewCount();
}
