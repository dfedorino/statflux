package com.rmrf.statflux.integration.rutube;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Вспомогательный класс, предоставляющий методы для проверки и парсинга RuTube-ссылки
 */
public class RutubeUrlParser {

    // https://rutube.ru/video/<32 hex chars> или https://m.rutube.ru/video/<32 hex chars>
    private static final Pattern PATTERN =
        Pattern.compile("https?://(?:m\\.)?rutube\\.ru/video/([a-f0-9]{32})");

    /**
     * Извлекает videoId из RuTube ссылки
     */
    public static String extractVideoId(String url) {
        if (url == null || url.isBlank()) {
            throw new BadUrlException("URL can`t be empty!");
        }
        String cleanUrl = url.split("#")[0];
        Matcher matcher = PATTERN.matcher(cleanUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new BadUrlException("Failed to get video ID from URL: " + url);
    }

    /**
     * Валидирует ссылку
     */
    public static boolean isValidRutubeUrl(String url) {
        try {
            extractVideoId(url);
            return true;
        } catch (BadUrlException e) {
            return false;
        }
    }
}
