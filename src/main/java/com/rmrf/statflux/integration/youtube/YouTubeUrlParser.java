package com.rmrf.statflux.integration.youtube;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Вспомогательный класс, предоставляющий методы для проверки и парсинга YouTube-ссылки
 */
public class YouTubeUrlParser {

    private static final Pattern[] PATTERNS = {
        // youtube.com/watch?v=VIDEO_ID или youtu.be/VIDEO_ID
        Pattern.compile("(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/)([a-zA-Z0-9_-]{11})"),

        // youtube.com/embed/VIDEO_ID
        Pattern.compile("youtube\\.com\\/embed\\/([a-zA-Z0-9_-]{11})"),

        // youtube.com/shorts/VIDEO_ID
        Pattern.compile("youtube\\.com\\/shorts\\/([a-zA-Z0-9_-]{11})"),

        // youtube.com/live/VIDEO_ID (прямой эфир)
        Pattern.compile("youtube\\.com\\/live\\/([a-zA-Z0-9_-]{11})"),

        // youtube.com/VIDEO_ID (короткая ссылка)
        Pattern.compile("youtube\\.com\\/([a-zA-Z0-9_-]{11})(?:\\?|$)"),};

    /**
     * Извлекает videoId из YouTube ссылки
     */
    public static String extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            throw new BadUrlException("URL can`t be empty!");
        }

        String cleanUrl = url.split("#")[0];

        for (Pattern pattern : PATTERNS) {
            Matcher matcher = pattern.matcher(cleanUrl);

            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        throw new BadUrlException("Failed to get video ID from URL: " + url);
    }

    /**
     * Валидирует ссылку
     */
    public static boolean isValidYouTubeUrl(String url) {
        try {
            extractVideoId(url);
            return true;
        } catch (BadUrlException e) {
            return false;
        }
    }
}
