package com.rmrf.statflux.bot.infra;

import com.rmrf.statflux.bot.port.TelegramBotConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class TelegramBotConfigFromEnv implements TelegramBotConfig {
    private String token;
    private Set<String> whiteList;

    public TelegramBotConfigFromEnv() {
        token = System.getenv("BOT_TOKEN");
        String whiteListStringified = System.getenv("BOT_WHITE_LIST");
        try {
            whiteList = Arrays.stream(whiteListStringified.split(",")).collect(Collectors.toSet());
        } catch (Throwable e) {
            whiteList = Collections.emptySet();
        }
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public Set<String> getWhiteList() {
        return whiteList;
    }
}
