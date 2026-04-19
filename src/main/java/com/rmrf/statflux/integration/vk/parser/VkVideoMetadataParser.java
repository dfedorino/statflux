package com.rmrf.statflux.integration.vk.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.rmrf.statflux.domain.dto.VideoMetadataResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VkVideoMetadataParser {

    /**
     * Фабрика JSON для создания потокового парсера Jackson.
     */
    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    /**
     * Имя корневого поля ответа VK API.
     */
    private static final String RESPONSE_FIELD = "response";

    /**
     * Поле с количеством элементов.
     */
    private static final String COUNT_FIELD = "count";

    /**
     * Поле с массивом объектов видео.
     */
    private static final String ITEMS_FIELD = "items";

    /**
     * Поле идентификатора владельца видео.
     */
    private static final String OWNER_ID_FIELD = "owner_id";

    /**
     * Поле идентификатора видео.
     */
    private static final String ID_FIELD = "id";

    /**
     * Поле заголовка видео.
     */
    private static final String TITLE_FIELD = "title";

    /**
     * Поле количества просмотров.
     */
    private static final String VIEWS_FIELD = "views";

    /**
     * Значение начальной ёмкости списка по умолчанию.
     */
    private static final int DEFAULT_CAPACITY = 8;

    /**
     * Парсит JSON-строку ответа VK API в список {@link VideoMetadataResponse}.
     *
     * @param json JSON-строка ответа VK API.
     * @return список объектов метаданных видео, либо пустой список при некорректном входе.
     */
    public List<VideoMetadataResponse> parse(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try (JsonParser parser = JSON_FACTORY.createParser(json)) {

            if (parser.nextToken() != JsonToken.START_OBJECT) {
                return List.of();
            }

            ArrayList<VideoMetadataResponse> result = null;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken() != JsonToken.FIELD_NAME) {
                    parser.skipChildren();
                    continue;
                }

                if (!RESPONSE_FIELD.equals(parser.currentName())) {
                    parser.nextToken();
                    parser.skipChildren();
                    continue;
                }

                if (parser.nextToken() == JsonToken.START_OBJECT) {
                    result = parseResponseObject(parser);
                } else {
                    parser.skipChildren();
                }

                break;
            }

            return result == null || result.isEmpty() ? List.of() : result;

        } catch (IOException exception) {
            log.error("Ошибка парсинга JSON VK API", exception);
            throw new IllegalArgumentException("Некорректный JSON VK видео", exception);
        }
    }

    /**
     * Парсит объект response из ответа VK API.
     *
     * @param parser JSON-парсер, установленный на начало объекта response
     * @return список DTO с метаданными видео
     * @throws IOException при ошибке чтения JSON
     */
    private ArrayList<VideoMetadataResponse> parseResponseObject(JsonParser parser)
        throws IOException {

        ArrayList<VideoMetadataResponse> result = null;
        int expectedCapacity = DEFAULT_CAPACITY;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken() != JsonToken.FIELD_NAME) {
                parser.skipChildren();
                continue;
            }

            String fieldName = parser.currentName();
            JsonToken valueToken = parser.nextToken();

            if (COUNT_FIELD.equals(fieldName) && valueToken.isNumeric()) {
                expectedCapacity = Math.max(parser.getIntValue(), DEFAULT_CAPACITY);
                continue;
            }

            if (ITEMS_FIELD.equals(fieldName) && valueToken == JsonToken.START_ARRAY) {
                result = parseItemsArray(parser, expectedCapacity);
                continue;
            }

            parser.skipChildren();
        }

        return result;
    }

    /**
     * Парсит массив объектов items.
     *
     * @param parser JSON-парсер, установленный на начало массива items
     * @param expectedCapacity ожидаемая ёмкость списка
     * @return список DTO с видео
     * @throws IOException при ошибке чтения JSON
     */
    private ArrayList<VideoMetadataResponse> parseItemsArray(JsonParser parser, int expectedCapacity)
        throws IOException {

        ArrayList<VideoMetadataResponse> result = new ArrayList<>(expectedCapacity);

        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                parser.skipChildren();
                continue;
            }

            VideoMetadataResponse item = parseSingleItem(parser);

            if (item != null) {
                result.add(item);
            }
        }

        return result;
    }

    /**
     * Парсит один объект видео.
     *
     * @param parser JSON-парсер, установленный на начало объекта видео
     * @return {@link VideoMetadataResponse} или null, если обязательные поля отсутствуют
     * @throws IOException при ошибке чтения JSON
     */
    private VideoMetadataResponse parseSingleItem(JsonParser parser)
        throws IOException {

        Long ownerId = null;
        Long videoId = null;
        String title = null;
        Long views = null;
        String videoUrl = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken() != JsonToken.FIELD_NAME) {
                parser.skipChildren();
                continue;
            }

            String fieldName = parser.currentName();
            JsonToken valueToken = parser.nextToken();

            if (valueToken == JsonToken.VALUE_NULL) {
                continue;
            }

            switch (fieldName) {

                case OWNER_ID_FIELD -> {
                    if (valueToken.isNumeric()) {
                        ownerId = parser.getLongValue();
                    }
                }

                case ID_FIELD -> {
                    if (valueToken.isNumeric()) {
                        videoId = parser.getLongValue();
                    }
                }

                case TITLE_FIELD -> {
                    if (valueToken == JsonToken.VALUE_STRING) {
                        title = parser.getText();
                    }
                }

                case VIEWS_FIELD -> {
                    if (valueToken.isNumeric()) {
                        views = parser.getLongValue();
                    }
                }

                default -> parser.skipChildren();
            }
        }

        if (ownerId == null || videoId == null || title == null) {
            return null;
        }

        return new VideoMetadataResponse(ownerId + "_" + videoId, title, views);
    }
}
