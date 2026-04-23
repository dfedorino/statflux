package com.rmrf.statflux.integration.vk.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class VkUrlParserTest {

    @Test
    @DisplayName("Extract video ID from valid VK URLs correctly")
    void extractId_ShouldReturnId_WhenValidUrl() {

        String videoId = "456239070";

        String[] urls = {
            "https://vk.com/video-113367061_" + videoId,
            "https://m.vk.com/video-113367061_" + videoId,
            "https://vkvideo.ru/video-113367061_" + videoId,
            "https://vkvideo.ru/video113367061_" + videoId,
            "https://vk.com/video113367061_" + videoId,
            "https://vkvideo.ru/video-113367061_" + videoId + "#section",
            "https://vkvideo.ru/video-113367061_" + videoId + "   "
        };

        String expectedWithMinus = "-113367061_" + videoId;
        String expectedWithoutMinus = "113367061_" + videoId;

        for (String url : urls) {
            String id = VkUrlParser.extractId(url);

            boolean valid =
                id.equals(expectedWithMinus) ||
                    id.equals(expectedWithoutMinus);

            assertTrue(valid, "Unexpected id: " + id);
        }
    }

    @Test
    @DisplayName("Throw exception for invalid VK URL")
    void extractId_ShouldThrowException_WhenInvalidUrl() {
        assertThrows(BadUrlException.class, () ->
            VkUrlParser.extractId("https://google.com")
        );

        assertThrows(BadUrlException.class, () ->
            VkUrlParser.extractId("https://youtube.com/watch?v=123123123")
        );
    }

    @Test
    @DisplayName("Validate VK URL correctly")
    void validate_ShouldReturnTrue_WhenValid() {
        assertTrue(VkUrlParser.validate("https://vkvideo.ru/video-113367061_456239070"));
        assertTrue(VkUrlParser.validate("https://vk.com/video113367061_456239070"));
        assertTrue(VkUrlParser.validate("https://m.vk.com/video-113367061_456239070"));

        assertFalse(VkUrlParser.validate("https://google.com"));
        assertFalse(VkUrlParser.validate("https://youtube.com/watch?v=test"));
    }
}
