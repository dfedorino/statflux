package com.rmrf.statflux.bot.infra.util;

import java.util.Locale;

public class TelegramBotFormatter {

    // Полный список спецсимволов MarkdownV2 по документации Telegram:
    // https://core.telegram.org/bots/api#markdownv2-style
    // Если не экранировать — Telegram вернёт 400 Bad Request при отправке сообщения.
    private static final String[] MARKDOWN_V2_SPECIAL_CHARS = {
            "\\", "_", "*", "[", "]", "(", ")", "~", "`", ">",
            "#", "+", "-", "=", "|", "{", "}", ".", "!"
    };

    /**
     * Экранирует все спецсимволы MarkdownV2 в пользовательском тексте.
     */
    public static String escapeSpecial(String text) {
        for (String ch : MARKDOWN_V2_SPECIAL_CHARS) {
            text = text.replace(ch, "\\" + ch);
        }
        return text;
    }

    /**
     * Группирует разряды числа пробелами по три: тысячи, миллионы и т.д.
     * Примеры: {@code 0 -> "0"}, {@code 1234 -> "1 234"},
     * {@code 1234567 -> "1 234 567"}, {@code -42 -> "-42"}.
     */
    public static String groupThousands(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
    }

    public static String groupThousands(int value) {
        return groupThousands((long) value);
    }
}