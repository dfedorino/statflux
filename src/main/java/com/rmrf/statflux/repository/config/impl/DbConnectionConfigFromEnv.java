package com.rmrf.statflux.repository.config.impl;

import com.rmrf.statflux.repository.config.DbConnectionConfig;

public class DbConnectionConfigFromEnv implements DbConnectionConfig {

    @Override
    public String getUrl() {
        return System.getProperty("DB_URL", System.getenv("DB_URL"));
    }

    @Override
    public String getUsername() {
        return System.getProperty("DB_USER", System.getenv("DB_USER"));
    }

    @Override
    public String getPassword() {
        return System.getProperty("DB_PASSWORD", System.getenv("DB_PASSWORD"));
    }
}
