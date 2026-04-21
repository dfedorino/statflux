package com.rmrf.statflux.integration.config;

import java.util.Properties;
import lombok.Getter;

/**
 * Параметры для слоя integration из application.properties
 * @deprecated В пользу env
 */
@Deprecated
@Getter
public class IntegrationConfigFromProperties implements IntegrationConfig {

    /**
     * Таймаут HTTP запроса.
     */
    private final int timeout;

    /**
     * YouTube API Ключ.
     */
    private final String youTubeApiKey;

    /**
     * VK API URL.
     */
    private final String vkApiUrl;

    /**
     * VK API Ключ.
     */
    private final String vkApiKey;

    /**
     * VK API Версия.
     */
    private final String vkApiVersion;

    /**
     * IntegrationConfig конструктор.
     */
    public IntegrationConfigFromProperties(Properties props) {
        this.timeout = Integer.parseInt(props.getProperty("http.timeout", "15"));

        this.youTubeApiKey = props.getProperty("youtube.key");

        this.vkApiUrl = props.getProperty("vk.api.url");
        this.vkApiKey = props.getProperty("vk.api.token");
        this.vkApiVersion = props.getProperty("vk.api.version");
    }
}
