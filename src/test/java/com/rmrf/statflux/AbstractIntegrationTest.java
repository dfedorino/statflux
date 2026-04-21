package com.rmrf.statflux;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("statflux_db")
            .withUsername("statflux")
            .withPassword("statflux")
            .withInitScript("schema.sql");

//    static {
//        POSTGRES.start();
//
//        System.setProperty("DB_URL", POSTGRES.getJdbcUrl());
//        System.setProperty("DB_USER", POSTGRES.getUsername());
//        System.setProperty("DB_PASSWORD", POSTGRES.getPassword());
//    }

    @BeforeAll
    static void init() {
        System.setProperty("DB_URL", POSTGRES.getJdbcUrl());
        System.setProperty("DB_USER", POSTGRES.getUsername());
        System.setProperty("DB_PASSWORD", POSTGRES.getPassword());
    }
}