package com.rmrf.statflux.integration.config;

import com.rmrf.statflux.integration.VideoProviderFactory;
import com.rmrf.statflux.integration.rutube.RutubeVideoProvider;
import com.rmrf.statflux.integration.rutube.RutubeVideoProviderImpl;
import com.rmrf.statflux.integration.utils.SimpleHttpClient;
import com.rmrf.statflux.integration.vk.VkVideoProvider;
import com.rmrf.statflux.integration.vk.VkVideoProviderImpl;
import com.rmrf.statflux.integration.youtube.YouTubeVideoProvider;
import com.rmrf.statflux.integration.youtube.YouTubeVideoProviderImpl;

/**
 * Конфигурация для слоя integration
 */
public class IntegrationLayerConfig {

    private final SimpleHttpClient simpleHttpClient;
    private final YouTubeVideoProviderImpl youTubeVideoProvider;
    private final VkVideoProvider vkVideoProvider;
    private final RutubeVideoProvider rutubeVideoProvider;
    private final VideoProviderFactory providerFactory;

    public IntegrationLayerConfig(IntegrationConfig config) {
        this.simpleHttpClient = new SimpleHttpClient(config.getTimeout());
        this.youTubeVideoProvider = new YouTubeVideoProviderImpl(config.getYouTubeApiKey(), simpleHttpClient);
        this.vkVideoProvider = new VkVideoProviderImpl(config.getVkApiUrl(), config.getVkApiKey(), config.getVkApiVersion(), simpleHttpClient);
        this.rutubeVideoProvider = new RutubeVideoProviderImpl(config.getRutubeApiUrl(), simpleHttpClient);
        this.providerFactory = new VideoProviderFactory(youTubeVideoProvider, vkVideoProvider, rutubeVideoProvider);
    }

    public SimpleHttpClient simpleHttpClient() {
        return simpleHttpClient;
    }

    public YouTubeVideoProvider youTubeVideoProvider() {
        return youTubeVideoProvider;
    }

    public VkVideoProvider vkVideoProvider() {
        return vkVideoProvider;
    }

    public RutubeVideoProvider rutubeVideoProvider() {
        return rutubeVideoProvider;
    }

    public VideoProviderFactory providerFactory() {
        return providerFactory;
    }
}
