package com.rmrf.statflux.bot.infra.util;

public class TelegramBotFormatter {

    /**
     * Экранирование спец. символов для тг
     */
    public static String escapeSpecial(String text) {
        return text.replace(".", "\\.")
            .replace("!", "\\!")
            .replace("=", "\\=")
            .replace("-", "\\-")
            .replace("+", "\\+")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace(":", "\\:")
            .replace("_", "\\_");
    }
}
