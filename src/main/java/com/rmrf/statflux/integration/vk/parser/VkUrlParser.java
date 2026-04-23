package com.rmrf.statflux.integration.vk.parser;

import com.rmrf.statflux.domain.exceptions.BadUrlException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Универсальный парсер VK Video ссылок.
 * <p>
 * Поддерживает следующие форматы:
 * <ul>
 *     <li>vk.com/video-123_456</li>
 *     <li>vk.com/video123_456</li>
 *     <li>vkvideo.ru/video-123_456</li>
 *     <li>vkvideo.ru/video123_456</li>
 *     <li>m.vk.com/video-123_456</li>
 *     <li>m.vk.com/video123_456</li>
 * </ul>
 */
public class VkUrlParser {

    /**
     * Ищет любую пару ownerId_videoId в URL. Работает независимо от домена и наличия "video-"
     * префикса.
     */
    private static final Pattern ID_PATTERN = Pattern.compile("(-?\\d+_\\d+)");

    /**
     * Извлекает ID видео VK.
     *
     * @param url VK Video URL.
     * @return строка вида ownerId_videoId (например -113367061_456239070).
     * @throws BadUrlException если URL невалидный или не распознан.
     */
    public static String extractId(String url) {
        if (url == null || url.isBlank()) {
            throw new BadUrlException("Empty URL");
        }

        String cleanUrl = url.trim();

        int hashIndex = cleanUrl.indexOf('#');

        if (hashIndex != -1) {
            cleanUrl = cleanUrl.substring(0, hashIndex);
        }

        cleanUrl = cleanUrl.replaceAll("\\s+", "");

        Matcher matcher = ID_PATTERN.matcher(cleanUrl);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new BadUrlException("Failed to extract VK video ID from URL: " + url);
    }

    /**
     * Проверка валидности VK Video URL.
     *
     * @param url входной URL.
     * @return true если ссылка распознана.
     */
    public static boolean validate(String url) {
        try {
            extractId(url);

            return true;
        } catch (BadUrlException e) {
            return false;
        }
    }
}
