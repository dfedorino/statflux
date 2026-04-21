package com.rmrf.statflux.bot.infra.config;

import com.rmrf.statflux.bot.infra.handler.CommandStartHandler;
import com.rmrf.statflux.bot.infra.handler.CommandStatsHandler;
import com.rmrf.statflux.bot.infra.handler.DefaultHandler;
import com.rmrf.statflux.bot.infra.handler.LinkHandler;
import com.rmrf.statflux.bot.infra.handler.NextCallbackHandler;
import com.rmrf.statflux.bot.infra.handler.PreviousCallbackHandler;
import com.rmrf.statflux.bot.infra.handler.RefreshCallbackHandler;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.bot.infra.middleware.UncaughtErrorMiddleware;
import com.rmrf.statflux.bot.infra.middleware.WhiteListMiddleware;
import com.rmrf.statflux.bot.port.TelegramBotConfig;
import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.integration.config.IntegrationConfig;
import com.rmrf.statflux.integration.config.IntegrationConfigFromEnv;
import com.rmrf.statflux.integration.config.IntegrationLayerConfig;
import com.rmrf.statflux.service.ServiceLayer;
import com.rmrf.statflux.service.config.ServiceConfig;

public class HandlersConfig {
    private TelegramBotConfig telegramBotConfig = new TelegramBotConfigFromEnv();
    private final IntegrationConfig integrationConfig = new IntegrationConfigFromEnv();
    private final IntegrationLayerConfig integrationLayerConfig = new IntegrationLayerConfig(integrationConfig);
    private final ServiceConfig serviceConfig = new ServiceConfig();
    private final Localization localization = ConfigLoader.loadL10n("l10n/ru.yaml", Localization.class);
    private final ServiceLayer serviceLayer = serviceConfig.serviceLayer(integrationLayerConfig.providerFactory());

    public RefreshCallbackHandler refreshCallbackHandler() {
        return new RefreshCallbackHandler(serviceLayer, localization);
    }

    public PreviousCallbackHandler previousCallbackHandler() {
        return new PreviousCallbackHandler(serviceLayer, localization);
    }

    public NextCallbackHandler nextCallbackHandler() {
        return new NextCallbackHandler(serviceLayer, localization);
    }

    public CommandStartHandler commandStartHandler() {
        return new CommandStartHandler(localization.start);
    }

    public LinkHandler linkHandler() {
        return new LinkHandler(serviceLayer, localization.link);
    }

    public CommandStatsHandler commandStatsHandler() {
        return new CommandStatsHandler(serviceLayer, localization);
    }

    public DefaultHandler defaultHandler() {
        return new DefaultHandler(localization.common);
    }

    public WhiteListMiddleware whiteListMiddleware() {
        return new WhiteListMiddleware(telegramBotConfig.getWhiteList());
    }

    public UncaughtErrorMiddleware uncaughtErrorMiddleware() {
        return new UncaughtErrorMiddleware(localization.common);
    }
}
