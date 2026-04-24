package com.rmrf.statflux;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("statflux_db")
            .withUsername("statflux")
            .withPassword("statflux")
            .withInitScript("schema.sql");

    @BeforeAll
    static void init() {
        System.setProperty("DB_URL", POSTGRES.getJdbcUrl());
        System.setProperty("DB_USER", POSTGRES.getUsername());
        System.setProperty("DB_PASSWORD", POSTGRES.getPassword());
    }
}