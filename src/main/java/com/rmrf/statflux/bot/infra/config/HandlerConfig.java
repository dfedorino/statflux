package com.rmrf.statflux.bot.infra.config;

import com.rmrf.statflux.bot.infra.handler.CommandStatsHandler;
import com.rmrf.statflux.bot.infra.l10n.Localization;
import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.integration.config.IntegrationConfig;
import com.rmrf.statflux.integration.config.IntegrationConfigFromEnv;
import com.rmrf.statflux.integration.config.IntegrationLayerConfig;
import com.rmrf.statflux.service.config.ServiceConfig;

public class HandlerConfig {
    private final IntegrationConfig integrationConfig = new IntegrationConfigFromEnv();
    private final IntegrationLayerConfig integrationLayerConfig = new IntegrationLayerConfig(integrationConfig);
    private final ServiceConfig serviceConfig = new ServiceConfig();
    private final Localization localization = ConfigLoader.loadL10n("l10n/ru.yaml", Localization.class);

    public CommandStatsHandler linkHandler() {
        return new CommandStatsHandler(serviceConfig.serviceLayer(integrationLayerConfig.providerFactory()), localization.stats);
    }
}
