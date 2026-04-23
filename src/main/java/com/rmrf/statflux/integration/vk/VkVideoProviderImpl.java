package com.rmrf.statflux.integration.vk;

import com.rmrf.statflux.domain.constant.Platform;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import com.rmrf.statflux.integration.vk.parser.VkVideoMetadataJsonParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VkVideoProviderImpl implements VkVideoProvider {

    /**
     * Максимальное обрабатываемое число видео за 1 запрос.
     */
    private static final int MAX_HANDLED_VIDEO = 50;

    /**
     * API URL.
     */
    private final String url;

    /**
     * API токен.
     */
    private final String token;

    /**
     * API версия.
     */
    private final String apiVersion;

    /**
     * SimpleHttpClient объект.
     */
    private final SimpleHttpClient httpClient;

    /**
     * VkVideoMetadataJsonParser объект.
     */
    private final VkVideoMetadataJsonParser parser = new VkVideoMetadataJsonParser();

    /**
     * Ограничение параллельных запросов к VK API.
     */
    private static final Semaphore VK_SEMAPHORE = new Semaphore(5);

    /**
     * VkVideoProviderImpl конструктор.
     *
     * @param url        API URL.
     * @param token      API токен.
     * @param apiVersion API версия.
     * @param httpClient HTTP клиент.
     */
    public VkVideoProviderImpl(
        String url,
        String token,
        String apiVersion,
        SimpleHttpClient httpClient
    ) {
        this.url = Objects.requireNonNull(url);
        this.token = Objects.requireNonNull(token);
        this.apiVersion = Objects.requireNonNull(apiVersion);
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    /**
     * Получает метаданные видео по одному идентификатору.
     *
     * @param id идентификатор видео.
     * @return объект {@link Result}, содержащий {@link VideoMetadataResponse}.
     */
    @Override
    public @NonNull Result<VideoMetadataResponse> metadataById(String id) {
        return getStatistic(id);
    }

    /**
     * Получает метаданные видео по списку идентификаторов.
     * <p>
     * Если список превышает {@link #MAX_HANDLED_VIDEO}, выполняется асинхронная обработка батчами.
     *
     * @param ids список идентификаторов видео.
     * @return объект {@link Result}, содержащий список {@link VideoMetadataResponse}.
     */
    @Override
    public @NonNull Result<List<VideoMetadataResponse>> metadataByIds(List<String> ids) {

        if (ids == null || ids.isEmpty()) {
            return Failure.of(new Exception("Empty Video IDs: " + ids));
        }

        if (ids.size() <= MAX_HANDLED_VIDEO) {
            return this.getStatistic(ids);
        }

        return this.getStatisticAsync(ids);
    }

    /**
     * Синхронно получает метаданные видео по списку идентификаторов.
     *
     * @param ids список идентификаторов видео.
     * @return объект {@link Result}, содержащий список {@link VideoMetadataResponse}.
     */
    private Result<List<VideoMetadataResponse>> getStatistic(List<String> ids) {

        String idsParam = this.buildIdsParam(ids);

        if (idsParam.isBlank()) {
            return Failure.of(new Exception("Empty Video IDs: " + ids));
        }

        Result<String> jsonResult = this.fetchStatisticsJson(idsParam);

        if (jsonResult.isFailure()) {
            return Failure.of(
                jsonResult.asFailure().exception()
            );
        }

        String json = jsonResult.get();

        if (json == null || json.isBlank()) {
            return Failure.of(new Exception("VK API empty response"));
        }

        try {
            List<VideoMetadataResponse> parsed = parser.parse(json);

            if (parsed != null && !parsed.isEmpty()) {
                return Success.of(parsed);
            }

            return Failure.of(new Exception("Empty parse result for ids=" + ids));
        } catch (Exception e) {
            log.error("Failed to parse vk response. ids={}", ids, e);
            return Failure.of(e);
        }
    }

    /**
     * Асинхронно получает метаданные видео по списку идентификаторов.
     * <p>
     * Список разбивается на батчи по {@link #MAX_HANDLED_VIDEO}, каждый батч обрабатывается
     * параллельно с использованием {@link CompletableFuture}.
     *
     * @param ids список идентификаторов видео.
     * @return объект {@link Result}, содержащий список {@link VideoMetadataResponse}.
     */
    private Result<List<VideoMetadataResponse>> getStatisticAsync(List<String> ids) {

        List<List<String>> batches = this.split(ids, MAX_HANDLED_VIDEO);

        List<CompletableFuture<Result<List<VideoMetadataResponse>>>> futures =
            batches.stream()
                .map(batch ->
                    CompletableFuture.<Result<List<VideoMetadataResponse>>>supplyAsync(() -> {
                        try {
                            VK_SEMAPHORE.acquire();

                            return this.getStatistic(batch);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();

                            return Failure.of(e);
                        } finally {
                            VK_SEMAPHORE.release();
                        }
                    })
                )
                .toList();

        List<VideoMetadataResponse> result = new ArrayList<>();

        for (CompletableFuture<Result<List<VideoMetadataResponse>>> future : futures) {
            Result<List<VideoMetadataResponse>> batchResult = future.join();

            if (batchResult.isFailure()) {
                return Failure.of(batchResult.asFailure().exception());
            }

            result.addAll(batchResult.get());
        }

        return Success.of(result);
    }

    /**
     * Разбивает список идентификаторов видео на батчи фиксированного размера.
     *
     * @param ids  список идентификаторов видео.
     * @param size максимальный размер батча.
     * @return список батчей идентификаторов видео.
     */
    private List<List<String>> split(List<String> ids, int size) {
        List<List<String>> result = new ArrayList<>();

        for (int i = 0; i < ids.size(); i += size) {
            result.add(
                ids.subList(
                    i,
                    Math.min(
                        i + size,
                        ids.size()
                    )
                )
            );
        }

        return result;
    }

    /**
     * Формирует строку параметров для VK API.
     *
     * @param ids список идентификаторов видео.
     * @return строка идентификаторов, разделённых запятыми.
     */
    private String buildIdsParam(List<String> ids) {
        return ids.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.joining(","));
    }

    /**
     * Выполняет HTTP-запрос к VK API.
     *
     * @param idsParam строка идентификаторов видео.
     * @return JSON-ответ VK API.
     */
    private Result<String> fetchStatisticsJson(String idsParam) {
        try {
            return Success.of(
                httpClient.get(
                    makeUrl(idsParam),
                    Map.of("Authorization", "Bearer " + token)
                )
            );
        } catch (Exception e) {
            log.error("Failed to fetch VK statistics. ids={}", idsParam, e);
            return Failure.of(e);
        }
    }

    /**
     * Формирует URL запроса VK API.
     *
     * @param videoIds строка идентификаторов видео.
     * @return полный URL запроса.
     */
    private String makeUrl(String videoIds) {
        return String.format(
            "%s/method/video.get?videos=%s&extended=0&v=%s",
            url, videoIds, apiVersion
        );
    }

    /**
     * Получает метаданные видео по одному идентификатору.
     *
     * @param id идентификатор видео.
     * @return объект {@link Result}, содержащий {@link VideoMetadataResponse}.
     */
    private Result<VideoMetadataResponse> getStatistic(String id) {

        Result<String> jsonResult = fetchStatisticsJson(id);

        if (jsonResult.isFailure()) {
            return Failure.of(jsonResult.asFailure().exception());
        }

        String json = jsonResult.get();

        if (json == null || json.isBlank()) {
            return Failure.of(new Exception("VK API empty response for id=" + id));
        }

        try {
            List<VideoMetadataResponse> parsed = parser.parse(json);

            if (parsed != null && !parsed.isEmpty()) {
                return Success.of(parsed.getFirst());
            }

            return Failure.of(new Exception("Empty parse result for id=" + id));
        } catch (Exception e) {
            log.error("Failed to parse VK response. id={}", id, e);
            return Failure.of(e);
        }
    }

    /**
     * Возвращает название платформы.
     *
     * @return название платформы VK.
     */
    @Override
    public @NonNull String hostingName() {
        return Platform.VK.getDisplayName();
    }

    /**
     * Устаревший метод получения данных по ссылке.
     *
     * @param rawLink ссылка на видео.
     * @return всегда {@code Failure}.
     */
    @Deprecated
    @Override
    public @NonNull Result<VideoMetadataResponse> metadataByLink(String rawLink) {
        return Failure.of(new UnsupportedOperationException("Deprecated"));
    }
}
