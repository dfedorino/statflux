package com.rmrf.statflux.integration.youtube;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class YouTubeUrlParserTest {
    @Test
    @DisplayName("Extract video ID from valid YouTube URLs correctly")
    void extractVideoId_ShouldReturnId_WhenValidUrl() {
        String[] urls = {
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            "https://youtu.be/dQw4w9WgXcQ",
            "https://youtube.com/shorts/dQw4w9WgXcQ"
        };

        for (String url : urls) {
            String id = YouTubeUrlParser.extractVideoId(url);
            assertEquals("dQw4w9WgXcQ", id);
        }
    }

    @Test
    @DisplayName("Throw exception for invalid YouTube URL")
    void extractVideoId_ShouldThrowException_WhenInvalidUrl() {
        assertThrows(BadUrlException.class, () ->
            YouTubeUrlParser.extractVideoId("https://google.com"));
    }

    @Test
    @DisplayName("Validate YouTube URL correctly")
    void isValidYouTubeUrl_ShouldReturnTrue_WhenValid() {
        assertTrue(YouTubeUrlParser.isValidYouTubeUrl("https://youtu.be/dQw4w9WgXcQ"));
        assertFalse(YouTubeUrlParser.isValidYouTubeUrl("https://google.com"));
    }
}
