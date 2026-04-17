package com.rmrf.statflux;

import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.domain.dto.ParseVideoResponse;
import com.rmrf.statflux.domain.exceptions.BadUrlException;
import com.rmrf.statflux.domain.exceptions.UnsupportedUrlException;
import com.rmrf.statflux.integration.HostingApiFactory;
import com.rmrf.statflux.integration.vk.VkHostingApi;
import com.rmrf.statflux.integration.youtube.YouTubeHostingApi;
import com.rmrf.statflux.util.result.Result;
import lombok.NonNull;
import org.junit.jupiter.api.Test;

public class HostingApiFactoryTest {

    private static final YouTubeHostingApi youtubeStub = new YouTubeHostingApi() {
        @Override
        public @NonNull Result<ParseVideoResponse> viewCount(String rawLink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull String hostingName() {
            throw new UnsupportedOperationException();
        }
    };

    private static final VkHostingApi vkStub = new VkHostingApi() {
        @Override
        public @NonNull Result<ParseVideoResponse> viewCount(String rawLink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NonNull String hostingName() {
            throw new UnsupportedOperationException();
        }
    };

    private final HostingApiFactory factory = new HostingApiFactory(youtubeStub, vkStub);

    @Test
    void returnsYouTubeHostingApiForCorrectYouTubeLinks() {
        var url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
        assertThat(factory.forUrl(url).contains(youtubeStub));
    }

    @Test
    void returnsYouTubeHostingApiForCorrectYouTubeLinksMobile() {
        var url = "https://m.youtube.com/watch?v=dQw4w9WgXcQ";
        assertThat(factory.forUrl(url).contains(youtubeStub));
    }

    @Test
    void returnsYouTubeHostingApiForCorrectYouTubeLinksShare() {
        var url = "https://youtu.be/dQw4w9WgXcQ?si=8ELD_NCsKOPzcfJ0";
        assertThat(factory.forUrl(url).contains(youtubeStub));
    }

    @Test
    void returnsVkHostingApiForCorrectVkLinks() {
        var url = "https://vkvideo.ru/video-213638597_456239147";
        assertThat(factory.forUrl(url).contains(vkStub));
    }

    @Test
    void returnsVkHostingApiForCorrectVkLinksMobile() {
        var url = "https://m.vkvideo.ru/video-213638597_456239147?from=search";
        assertThat(factory.forUrl(url).contains(vkStub));
    }

    @Test
    void returnsVkHostingApiForCorrectVkLinksShare() {
        var url = "https://vkvideo.ru/video-213638597_456239147?list=ln-ZJlWY456pSfIkF5EYt";
        assertThat(factory.forUrl(url).contains(vkStub));
    }

    @Test
    void returnsNoneForIncorrectLints() {
        assertThat(factory.forUrl("https://example.com/video/123").isFailure());
        assertThat(
            factory.forUrl("https://example.com/video/123").asFailure().exception()).isInstanceOf(
            UnsupportedUrlException.class);
        assertThat(factory.forUrl("totallyunintelligibleurl").isFailure());
        assertThat(factory.forUrl("totallyunintelligibleurl").asFailure().exception()).isInstanceOf(
            BadUrlException.class);
    }
}
