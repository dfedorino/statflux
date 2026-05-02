package com.rmrf.statflux;

import com.rmrf.statflux.bot.core.TelegramBotRootConsumer;
import com.rmrf.statflux.bot.infra.config.TelegramBotConfigFromEnv;
import com.rmrf.statflux.bot.infra.config.HandlersConfig;
import com.rmrf.statflux.bot.port.TelegramBotConfig;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotConfig botConfig = new TelegramBotConfigFromEnv();

        HandlersConfig handlersConfig = new HandlersConfig();

        TelegramBotRootConsumer botRootConsumer = TelegramBotRootConsumer.builder()
                .withClient(new OkHttpTelegramClient(botConfig.getToken()))
                .use(handlersConfig.whiteListMiddleware())
                .use(handlersConfig.uncaughtErrorMiddleware())
                .use(handlersConfig.refreshCallbackHandler())
                .use(handlersConfig.previousCallbackHandler())
                .use(handlersConfig.nextCallbackHandler())
                .use(handlersConfig.commandStartHandler())
                .use(handlersConfig.commandStatsHandler())
                .use(handlersConfig.linkHandler())
                .use(handlersConfig.commandDeleteHandler())
                .use(handlersConfig.defaultHandler())
                .build();

        TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
        app.registerBot(botConfig.getToken(), botRootConsumer);
    }
}
