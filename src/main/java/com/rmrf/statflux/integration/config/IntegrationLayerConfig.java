package com.rmrf.statflux.integration.config;

import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.integration.youtube.YouTubeHostingApi;
import com.rmrf.statflux.integration.youtube.YouTubeHostingApiImpl;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;

/**
 * Конфигурация для слоя integration
 */
public class IntegrationLayerConfig {

    private final SimpleHttpClient simpleHttpClient;
    private final YouTubeHostingApi youTubeHostingApi;
    private final HostingApiFactory hostingApiFactory;

    public IntegrationLayerConfig(IntegrationConfig config) {
        this.simpleHttpClient = new SimpleHttpClient(config.getTimeout());
        this.youTubeHostingApi = new YouTubeHostingApiImpl(config.getApiKey(), simpleHttpClient);
        this.hostingApiFactory = new HostingApiFactory(youTubeHostingApi, null);
    }

    public SimpleHttpClient simpleHttpClient() {
        return simpleHttpClient;
    }

    public YouTubeHostingApi youTubeHostingApi() {
        return youTubeHostingApi;
    }

    public HostingApiFactory hostingApiFactory() {
        return hostingApiFactory;
    }
}