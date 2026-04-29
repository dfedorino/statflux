package com.rmrf.statflux.integration.rutube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RutubeUrlParserTest {

    @Test
    @DisplayName("Extract video ID from valid RuTube URLs correctly")
    void extractVideoId_ShouldReturnId_WhenValidUrl() {
        String[] urls = {
            "https://rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab/",
            "https://rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab",
            "https://rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab/?p=abcd",
            "https://m.rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab/"
        };

        for (String url : urls) {
            String id = RutubeUrlParser.extractVideoId(url);
            assertEquals("10b3a03fc01d5bbcc632a2f3514e8aab", id);
        }
    }

    @Test
    @DisplayName("Throw exception for invalid RuTube URL")
    void extractVideoId_ShouldThrowException_WhenInvalidUrl() {
        assertThrows(BadUrlException.class, () ->
            RutubeUrlParser.extractVideoId("https://home.rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab/"));
        assertThrows(BadUrlException.class, () ->
            RutubeUrlParser.extractVideoId("https://get.rutube.com/video/10b3a03fc01d5bbcc632a2f3514e8aab/"));
    }

    @Test
    @DisplayName("Return true for valid RuTube URL")
    void isValidRutubeUrl_ShouldReturnTrue_WhenValid() {
        assertTrue(RutubeUrlParser.isValidRutubeUrl(
            "https://rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab/"));
        assertTrue(RutubeUrlParser.isValidRutubeUrl(
            "https://m.rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab/"));
    }

    @Test
    @DisplayName("Return false for invalid RuTube URL")
    void isValidRutubeUrl_ShouldReturnFalse_WhenInvalid() {
        assertFalse(RutubeUrlParser.isValidRutubeUrl(
            "https://home.rutube.ru/video/10b3a03fc01d5bbcc632a2f3514e8aab/"));
        assertFalse(RutubeUrlParser.isValidRutubeUrl(
            "https://get.rutube.com/video/10b3a03fc01d5bbcc632a2f3514e8aab/"));
    }
}
