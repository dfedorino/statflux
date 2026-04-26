package com.rmrf.statflux.bot.infra.util;

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
}