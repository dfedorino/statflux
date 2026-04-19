package com.rmrf.statflux;

import static com.rmrf.statflux.Stubs.vkStub;
import static com.rmrf.statflux.Stubs.youtubeStub;
import static org.assertj.core.api.Assertions.assertThat;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import com.rmrf.statflux.domain.exceptions.UnsupportedUrlException;
import com.rmrf.statflux.integration.HostingApiFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class HostingApiFactoryTest {

    private final HostingApiFactory factory = new HostingApiFactory(youtubeStub, vkStub);

    @ParameterizedTest(name = "{0}")
    @MethodSource("youtubeUrls")
    void returnsYouTubeHostingApiForCorrectYouTubeLinks(String description, String url) {
        assertThat(factory.forUrl(url).contains(youtubeStub));
    }

    private static Stream<Arguments> youtubeUrls() {
        return Stream.of(
            Arguments.of("correct YouTube link", "https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
            Arguments.of("correct mobile YouTube link",
                "https://m.youtube.com/watch?v=dQw4w9WgXcQ"),
            Arguments.of("correct shared YouTube link",
                "https://youtu.be/dQw4w9WgXcQ?si=8ELD_NCsKOPzcfJ0")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("vkUrls")
    void returnsVkHostingApiForCorrectVkLinks(String description, String url) {
        assertThat(factory.forUrl(url).contains(vkStub));
    }

    private static Stream<Arguments> vkUrls() {
        return Stream.of(
            Arguments.of("correct VK Video link", "https://vkvideo.ru/video-213638597_456239147"),
            Arguments.of("correct mobileVK Video link",
                "https://m.vkvideo.ru/video-213638597_456239147?from=search"),
            Arguments.of("correct shared VK Video link",
                "https://vkvideo.ru/video-213638597_456239147?list=ln-ZJlWY456pSfIkF5EYt")
        );
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
