package com.rmrf.statflux.repository;

import com.rmrf.statflux.repository.config.RepositoryConfig;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.transaction.TransactionManager;
import com.rmrf.statflux.repository.util.SqlScripts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseRepositoryTest {
    protected RepositoryConfig repositoryConfig;
    protected DataSource dataSource;
    protected TransactionManager tx;

    @BeforeEach
    void setUp() {
        repositoryConfig = new RepositoryConfig();
        dataSource = repositoryConfig.simpleDataSource();
        tx = new TransactionManager(dataSource);
        tx.executeWithoutResult(() -> SqlScripts.run("schema.sql"));
    }

    @AfterEach
    void tearDown() {
        dataSource.close();
    }


}
