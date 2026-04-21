package com.rmrf.statflux.repository.config;

public interface DbPooledConnectionConfig extends DbConnectionConfig {
    int getPoolSize();
}
