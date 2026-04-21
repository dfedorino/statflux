package com.rmrf.statflux;

import com.rmrf.statflux.bot.core.TelegramBotRootConsumer;
import com.rmrf.statflux.bot.infra.config.HandlerConfig;
import com.rmrf.statflux.bot.infra.TelegramBotConfigFromEnv;
import com.rmrf.statflux.bot.infra.handler.CommandStartHandler;
import com.rmrf.statflux.bot.infra.handler.DefaultHandler;
import com.rmrf.statflux.bot.infra.handler.LinkHandler;
import com.rmrf.statflux.bot.infra.handler.NextCallbackHandler;
import com.rmrf.statflux.bot.infra.handler.PreviousCallbackHandler;
import com.rmrf.statflux.bot.infra.handler.RefreshCallbackHandler;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.bot.infra.middleware.WhiteListMiddleware;
import com.rmrf.statflux.bot.port.TelegramBotConfig;
import com.rmrf.statflux.common.ConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class Main {
    public static void main(String[] args) throws TelegramApiException {
        TelegramBotConfig botConfig = new TelegramBotConfigFromEnv();
        Localization localization = ConfigLoader.loadL10n("l10n/ru.yaml", Localization.class);

        HandlerConfig handlerConfig = new HandlerConfig();

        TelegramBotRootConsumer botRootConsumer = TelegramBotRootConsumer.builder()
                .withClient(new OkHttpTelegramClient(botConfig.getToken()))
                .use(new WhiteListMiddleware(botConfig.getWhiteList()))
                .use(new RefreshCallbackHandler())
                .use(new PreviousCallbackHandler())
                .use(new NextCallbackHandler())
                .use(new CommandStartHandler(localization.start))
                .use(handlerConfig.linkHandler())
                .use(new LinkHandler(localization.link))
                .use(new DefaultHandler(localization.common))
                .build();

        TelegramBotsLongPollingApplication app = new TelegramBotsLongPollingApplication();
        app.registerBot(botConfig.getToken(), botRootConsumer);
    }
}
