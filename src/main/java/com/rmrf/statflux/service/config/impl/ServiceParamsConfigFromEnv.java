package com.rmrf.statflux.service.config.impl;

import com.rmrf.statflux.service.config.ServiceParamsConfig;

public class ServiceParamsConfigFromEnv implements ServiceParamsConfig {
    private final String refreshDelayMs = System.getenv("REFRESH_DELAY_MS");
    private final String pageSize = System.getenv("PAGE_SIZE");

    @Override
    public long getRefreshDelayMs() {
        return Long.parseLong(refreshDelayMs);
    }

    @Override
    public int getPageSize() {
        return Integer.parseInt(pageSize);
    }
}
