package com.rmrf.statflux.integration;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import com.rmrf.statflux.domain.exceptions.UnsupportedUrlException;
import com.rmrf.statflux.integration.vk.VkHostingApi;
import com.rmrf.statflux.integration.youtube.YouTubeHostingApi;
import com.rmrf.statflux.util.result.Failure;
import com.rmrf.statflux.util.result.Result;
import com.rmrf.statflux.util.result.Success;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HostingApiFactory {

    private final YouTubeHostingApi youTubeHostingApi;
    private final VkHostingApi vkHostingApi;

    public HostingApiFactory(YouTubeHostingApi youTubeHostingApi, VkHostingApi vkHostingApi) {
        this.youTubeHostingApi = youTubeHostingApi;
        this.vkHostingApi = vkHostingApi;
    }

    public Result<HostingApi> forUrl(String rawUrl) {
        try {
            var host = URI.create(rawUrl).getHost().toLowerCase();
            if (host.contains("youtube") || host.contains("youtu.be")) {
                return Success.of(youTubeHostingApi);
            }
            if (host.contains("vkvideo") || host.contains("vk.com") || host.contains("vk.ru")) {
                return Success.of(vkHostingApi);
            }
            return Failure.of(new UnsupportedUrlException(rawUrl));
        } catch (Exception e) {
            log.error("HostingApiFactory[forUrl] uncaught exception rawUrl={}", rawUrl, e);
            return Failure.of(new BadUrlException(rawUrl));
        }
    }
}
