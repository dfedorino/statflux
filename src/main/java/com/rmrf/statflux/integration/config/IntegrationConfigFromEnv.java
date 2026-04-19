package com.rmrf.statflux.integration.config;

import java.util.Properties;

/**
 * Параметры для слоя integration из переменных окружения
 */
public class IntegrationConfigFromEnv implements IntegrationConfig {
    private final String apiKey;
    private final int timeout;

    public IntegrationConfigFromEnv(Properties props) {
        this.apiKey = System.getenv("YOUTUBE_KEY");
        this.timeout = Integer.parseInt(System.getenv("HTTP_TIMEOUT"));
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
