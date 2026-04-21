package com.rmrf.statflux.bot.infra;

import com.rmrf.statflux.bot.port.TelegramBotConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @deprecated В пользу env
 */
@Deprecated
public class TelegramBotConfigFromProperties implements TelegramBotConfig {
    private final String token;
    private Set<String> whiteList;

    public TelegramBotConfigFromProperties(Properties properties) {
        token = properties.getProperty("bot.token");
        String whiteListStringified = properties.getProperty("bot.white-list");
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
