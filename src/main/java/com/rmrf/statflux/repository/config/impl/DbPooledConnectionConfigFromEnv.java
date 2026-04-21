package com.rmrf.statflux.repository.config.impl;

import com.rmrf.statflux.repository.config.DbPooledConnectionConfig;

public class DbPooledConnectionConfigFromEnv extends DbConnectionConfigFromEnv implements DbPooledConnectionConfig {
    private final String poolSize = System.getenv("POOL_SIZE");

    @Override
    public int getPoolSize() {
        return Integer.parseInt(poolSize);
    }
}
