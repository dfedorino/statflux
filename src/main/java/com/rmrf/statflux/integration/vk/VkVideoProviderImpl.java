package com.rmrf.statflux.integration.vk;

import com.rmrf.statflux.domain.constant.Platform;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import com.rmrf.statflux.integration.vk.parser.VkVideoMetadataJsonParser;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VkVideoProviderImpl implements VkVideoProvider {

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
     * VkVideoMetadataParser объект.
     */
    private final VkVideoMetadataJsonParser parser = new VkVideoMetadataJsonParser();

    /**
     * VkVideoStatsProviderImpl конструктор.
     *
     * @param url        API URL.
     * @param token      API токен.
     * @param apiVersion API версия.
     * @param httpClient SimpleHttpClient объект.
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
     * Получить информацию о статистике просмотров, название и ID видео в объекте {@link Result},
     * содержащем {@link VideoMetadataResponse}.
     *
     * @param id ID видео.
     * @return объект {@link Result}, содержащем {@link VideoMetadataResponse}.
     */
    @Override
    public @NonNull Result<VideoMetadataResponse> metadataById(String id) {
        log.debug("Input video Id: id={}", id);

        return this.getStatistic(id);
    }

    /**
     * Возвращает список видео со статистикой просмотров, названием и ID.
     *
     * @param ids список идентификаторов видео.
     * @return список видео с собранными данными - {@link VideoMetadataResponse}
     */
    @Override
    public @NonNull Result<List<VideoMetadataResponse>> metadataByIds(List<String> ids) {
        // разделяем по батчам по 50 штук
        // считаем число батчей
        // запускаем async пачки
        // сливаем результаты в list

        log.debug("Input video Ids: ids={}", ids);

        return this.getStatistic(ids);
    }

    /**
     * Получает статистику по списку идентификаторов видео.
     *
     * @param ids список ID видео.
     * @return список объектов статистики - {@link VideoMetadataResponse}; пустой список в случае
     * отсутствия данных или ошибки.
     */
    private Result<List<VideoMetadataResponse>> getStatistic(List<String> ids) {
        String idsParam = buildIdsParam(ids);

        if (idsParam.isBlank()) {
            return Success.of(List.of());
        }

        Result<String> jsonResult = this.fetchStatisticsJson(idsParam);

        if (jsonResult.isFailure()) {
            return Failure.of(
                jsonResult.asFailure().exception()
            );
        }

        String json = jsonResult.get();

        if (json == null || json.isBlank()) {
            return Success.of(List.of());
        }

        try {
            List<VideoMetadataResponse> parsed = this.parser.parse(json);

            return Success.of(
                parsed != null ? parsed : List.of()
            );
        } catch (Exception e) {
            log.error("Failed to parse vk response. ids={}", ids, e);
            return Failure.of(e);
        }
    }

    /**
     * Получает статистику по идентификатору видео.
     *
     * @param id ID видео.
     * @return объект {@link Result}, внутри которого - {@link VideoMetadataResponse}.
     */
    private Result<VideoMetadataResponse> getStatistic(String id) {
        Result<String> jsonResult = this.fetchStatisticsJson(id);

        if (jsonResult.isFailure()) {
            return Failure.of(
                jsonResult.asFailure().exception()
            );
        }

        String json = jsonResult.get();
        log.debug("vk response: {}", json);

        if (json == null || json.isBlank()) {
            return Failure.of(
                new Exception("VK video not found: " + id)
            );
        }

        try {
            List<VideoMetadataResponse> parsed = this.parser.parse(json);

            log.debug("parsed data: {}", parsed);

            if (parsed != null && !parsed.isEmpty()) {
                return Success.of(parsed.getFirst());
            }

            return Failure.of(
                new Exception(
                    "VK parse returned empty result for id=" + id + "; parsed data=" + parsed
                )
            );
        } catch (Exception exception) {
            log.error("Failed to parse vk response. id={}", id, exception);
            return Failure.of(exception);
        }
    }

    /**
     * Формируем строку для параметра videos.
     *
     * @param ids список ID запрашиваемых видео.
     * @return возвращает строку, содержащую идентификаторы видео, разделенные запятыми.
     */
    private String buildIdsParam(List<String> ids) {
        return ids.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.joining(","));
    }

    /**
     * Получает статистику по списку идентификаторов видео.
     *
     * @param idsParam строка с ID видео, разделёнными запятыми.
     * @return JSON-ответ со статистикой или пустая строка в случае ошибки.
     */
    private Result<String> fetchStatisticsJson(String idsParam) {
        try {
            return Success.of(
                this.httpClient.get(
                    this.makeUrl(idsParam),
                    Map.of("Authorization", "Bearer " + this.token)
                )
            );
        } catch (Exception exception) {
            log.error("Failed to fetch statistics. ids={}", idsParam, exception);
            return Failure.of(exception);
        }
    }

    /**
     * Формирует полный URL запроса.
     *
     * @param videoIds строка с ID видео, разделёнными запятыми.
     * @return полный URL запроса.
     */
    private String makeUrl(String videoIds) {
        return String.format(
            "%s/method/video.get?videos=%s&extended=0&v=%s",
            this.url, videoIds, this.apiVersion
        );
    }

    /**
     * Возвращает название провайдера.
     *
     * @return название провайдера.
     */
    @Override
    public @NonNull String hostingName() {
        return Platform.VK.getDisplayName();
    }









    @Deprecated
    @Override
    public @NonNull Result<VideoMetadataResponse> metadataByLink(String rawLink) {

        // TODO: убрать
        return null;
    }
}
