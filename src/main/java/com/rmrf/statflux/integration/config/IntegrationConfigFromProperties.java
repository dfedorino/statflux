package com.rmrf.statflux.integration.config;

import java.util.Properties;

/**
 * Параметры для слоя integration из application.properties
 */
public class IntegrationConfigFromProperties implements IntegrationConfig {
    private final String apiKey;
    private final int timeout;

    public IntegrationConfigFromProperties(Properties props) {
        this.apiKey = props.getProperty("youtube.key");
        this.timeout = Integer.parseInt(props.getProperty("http.timeout", "15"));
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }
}
