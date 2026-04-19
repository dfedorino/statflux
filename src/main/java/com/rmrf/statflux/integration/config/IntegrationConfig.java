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

    public IntegrationConfig() {
        Properties props = ConfigLoader.load("application.properties");
        this.apiKey = props.getProperty("youtube.key");
        this.timeout = Integer.parseInt(props.getProperty("http.timeout", "15"));
    }
}
