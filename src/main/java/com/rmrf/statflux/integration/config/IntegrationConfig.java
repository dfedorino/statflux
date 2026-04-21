package com.rmrf.statflux.integration.config;

/**
 * Общий контракт конфигурации интеграционного слоя.
 */
public interface IntegrationConfig {

    /**
     * Возвращает таймаут HTTP-запросов.
     *
     * @return таймаут в секундах
     */
    int getTimeout();

    /**
     * Возвращает API-ключ для YouTube.
     *
     * @return API-ключ или {@code null}, если не задан
     */
    String getYouTubeApiKey();

    /**
     * Возвращает базовый URL VK API.
     *
     * @return URL VK API (не {@code null})
     */
    String getVkApiUrl();

    /**
     * Возвращает токен доступа VK API.
     *
     * @return токен VK API (не {@code null})
     */
    String getVkApiKey();

    /**
     * Возвращает версию VK API.
     *
     * @return версия VK API (не {@code null})
     */
    String getVkApiVersion();
}
