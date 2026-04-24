package com.rmrf.statflux.integration.config;

import lombok.Getter;

/**
 * Параметры для слоя integration из переменных окружения
 */
@Getter
public class IntegrationConfigFromEnv implements IntegrationConfig {

    /**
     * Таймаут HTTP-запроса в секундах.
     */
    private final int timeout;

    /**
     * API-ключ YouTube.
     */
    private final String youTubeApiKey;

    /**
     * Базовый URL VK API.
     */
    private final String vkApiUrl;

    /**
     * Токен доступа VK API.
     */
    private final String vkApiKey;

    /**
     * Версия VK API.
     */
    private final String vkApiVersion;

    /**
     * IntegrationConfigFromEnv конструктор.
     */
    public IntegrationConfigFromEnv() {
        this.timeout = this.parseIntOrDefault(System.getenv("HTTP_TIMEOUT"), 15);

        this.youTubeApiKey = System.getenv("YOUTUBE_API_KEY");

        this.vkApiUrl = this.requireEnv("VK_API_URL");
        this.vkApiKey = this.requireEnv("VK_API_TOKEN");
        this.vkApiVersion = this.requireEnv("VK_API_VERSION");
    }

    /**
     * Парсит строку в int с возможностью возврата значения по умолчанию.
     *
     * @param value строковое значение переменной окружения.
     * @param defaultValue значение по умолчанию.
     * @return распаршенное число или {@code defaultValue}, если парсинг невозможен.
     */
    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Возвращает обязательную переменную окружения.
     *
     * @param name имя переменной.
     * @return значение переменной.
     * @throws IllegalStateException если переменная отсутствует или пуста.
     */
    private String requireEnv(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required");
        }

        return value;
    }
}
