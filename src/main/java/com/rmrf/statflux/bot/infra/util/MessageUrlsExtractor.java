package com.rmrf.statflux.bot.infra.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.message.Message;

/**
 * Достаёт все URL-кандидаты из текста Telegram-сообщения.
 *
 * <p>Источники: regex по тексту ({@code https?://\S+}) плюс entities типа
 * {@code text_link} (скрытые ссылки markdown-формата, которые regex не видит).
 * Кандидаты упорядочиваются по позиции в исходном тексте, режутся по look-ahead
 * на {@code https?://} (склейка без разделителя), хвостовая пунктуация
 * ({@code .,;:!?)]}»"'}) срезается. Дубликаты удаляются с сохранением порядка.
 */
public final class MessageUrlsExtractor {

    private static final String ENTITY_TEXT_LINK = "text_link";

    private static final Pattern URL_PATTERN =
        Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    private static final Pattern URL_BOUNDARY =
        Pattern.compile("(?=https?://)", Pattern.CASE_INSENSITIVE);

    // Срезаем только то, что точно не часть URL: пробелы, конец предложения,
    // закрывающие скобки/кавычки. {@code /?=&#} не трогаем — они валидны в URL.
    private static final String TRIM_TAIL = " \t\n\r.,;:!?)]}>«»\"'";

    private record Located(int offset, String value) {
    }

    private MessageUrlsExtractor() {
    }

    public static List<String> extract(Message message) {
        if (message == null || !message.hasText()) {
            return List.of();
        }

        String text = message.getText();
        List<Located> found = new ArrayList<>();

        Matcher m = URL_PATTERN.matcher(text);
        while (m.find()) {
            found.add(new Located(m.start(), m.group()));
        }

        if (message.hasEntities()) {
            for (MessageEntity e : message.getEntities()) {
                if (!ENTITY_TEXT_LINK.equals(e.getType())) {
                    continue;
                }
                String url = e.getUrl();
                if (url == null || url.isBlank()) {
                    continue;
                }
                Integer offset = e.getOffset();
                found.add(new Located(offset != null ? offset : Integer.MAX_VALUE, url));
            }
        }

        found.sort(Comparator.comparingInt(Located::offset));

        Set<String> dedup = new LinkedHashSet<>();
        for (Located f : found) {
            for (String piece : URL_BOUNDARY.split(f.value())) {
                String cleaned = trimTail(piece);
                if (startsWithHttp(cleaned)) {
                    dedup.add(cleaned);
                }
            }
        }

        return new ArrayList<>(dedup);
    }

    private static boolean startsWithHttp(String s) {
        return s.length() >= 7 && s.regionMatches(true, 0, "http", 0, 4);
    }

    private static String trimTail(String s) {
        int end = s.length();
        while (end > 0 && TRIM_TAIL.indexOf(s.charAt(end - 1)) >= 0) {
            end--;
        }
        return s.substring(0, end);
    }
}
