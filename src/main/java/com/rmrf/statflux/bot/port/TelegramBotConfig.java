package com.rmrf.statflux.bot.port;

import java.util.Set;

public interface TelegramBotConfig {
    /**
     * Токен для Telegram Bot API
     */
    String getToken();

    /**
     * Юзернеймы пользователей, с которыми бот может взаимодействовать
     */
    Set<String> getWhiteList();
}
