package com.rmrf.statflux.integration;

import com.rmrf.statflux.domain.constant.Platform;
import com.rmrf.statflux.domain.exceptions.BadUrlException;
import com.rmrf.statflux.domain.exceptions.UnsupportedPlatform;
import com.rmrf.statflux.domain.exceptions.UnsupportedUrlException;
import com.rmrf.statflux.domain.result.Failure;
import com.rmrf.statflux.domain.result.Result;
import com.rmrf.statflux.domain.result.Success;
import com.rmrf.statflux.integration.vk.VkVideoProvider;
import com.rmrf.statflux.integration.youtube.YouTubeUrlParser;
import com.rmrf.statflux.integration.youtube.YouTubeVideoProvider;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VideoProviderFactory {

    private final YouTubeVideoProvider youTubeVideoProvider;
    private final VkVideoProvider vkVideoProvider;

    public VideoProviderFactory(YouTubeVideoProvider youTubeVideoProvider, VkVideoProvider vkVideoProvider) {
        this.youTubeVideoProvider = youTubeVideoProvider;
        this.vkVideoProvider = vkVideoProvider;
    }

    public Result<VideoProvider> forUrl(String rawUrl) {
        try {
            if (YouTubeUrlParser.isValidYouTubeUrl(rawUrl)) {
                return Success.of(youTubeVideoProvider);
            }

            var host = URI.create(rawUrl).getHost().toLowerCase();
            if (host.contains("vkvideo") || host.contains("vk.com") || host.contains("vk.ru")) {
                return Success.of(vkVideoProvider);
            }
            return Failure.of(new UnsupportedUrlException(rawUrl));
        } catch (Exception e) {
            log.error("HostingApiFactory[forUrl] uncaught exception rawUrl={}", rawUrl, e);
            return Failure.of(new BadUrlException(rawUrl));
        }
    }

    public Result<VideoProvider> forUrl(Platform platform) {
        if (platform == Platform.YOUTUBE) {
            return Success.of(youTubeVideoProvider);
        }

        if (platform == Platform.VK) {
            return Success.of(vkVideoProvider);
        }

        return Failure.of(new UnsupportedPlatform("Unsupported platform!"));
    }
}
