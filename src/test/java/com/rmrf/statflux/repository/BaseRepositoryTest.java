package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.datasource.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseRepositoryTest {
    protected RepositoryConfig repositoryConfig;
    protected DataSource dataSource;

    @BeforeEach
    void setUp() {
        repositoryConfig = new RepositoryConfig();
        dataSource = repositoryConfig.dataSource();
        repositoryConfig.sqlScriptRunner().run("schema.sql");
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }


}
