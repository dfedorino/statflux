package com.rmrf.statflux.integration.config;

import com.rmrf.statflux.common.ConfigLoader;
import java.util.Properties;
import lombok.Getter;

/**
 * Параметры для слоя integration из application.properties
 */
@Getter
public class IntegrationConfig {

    private final String apiKey;
    private final int timeout;
    private final String vkApiKey;
    private final String vkApiUrl;
    private final String vkApiVersion;

    public IntegrationConfig() {
        Properties props = ConfigLoader.load("application.properties");
        this.apiKey = props.getProperty("youtube.key");
        this.vkApiKey = props.getProperty("vk.api.token");
        this.vkApiUrl = props.getProperty("vk.api.url");
        this.vkApiVersion = props.getProperty("vk.api.version");
        this.timeout = Integer.parseInt(props.getProperty("http.timeout", "15"));
    }
}
