package com.rmrf.statflux.repository.config.impl;

import com.rmrf.statflux.repository.config.DbConnectionConfig;

public class DbConnectionConfigFromEnv implements DbConnectionConfig {
    private final String url = System.getenv("DB_URL");
    private final String username = System.getenv("DB_USER");
    private final String password = System.getenv("DB_PASSWORD");

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
}
