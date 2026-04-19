package com.rmrf.statflux;

import com.rmrf.statflux.bot.core.TelegramBotRootConsumer;
import com.rmrf.statflux.bot.infra.TelegramBotConfigFromEnv;
import com.rmrf.statflux.bot.infra.handler.EchoHandler;
import com.rmrf.statflux.bot.infra.middleware.WhiteListMiddleware;
import com.rmrf.statflux.bot.port.TelegramBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotConfig botConfig = new TelegramBotConfigFromEnv();

        TelegramBotRootConsumer botRootConsumer = TelegramBotRootConsumer.builder()
                .withClient(new OkHttpTelegramClient(botConfig.getToken()))
                .use(new WhiteListMiddleware(botConfig.getWhiteList()))
                .use(new EchoHandler())
                .build();

        TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
        app.registerBot(botConfig.getToken(), botRootConsumer);
    }
}
