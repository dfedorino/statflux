package com.rmrf.statflux.repository.config;

import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.repository.JdbcLinkRepository;
import com.rmrf.statflux.repository.JdbcPaginationStateRepository;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.config.impl.DbConnectionConfigFromEnv;
import com.rmrf.statflux.repository.config.impl.DbPooledConnectionConfigFromEnv;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.datasource.impl.PooledDataSource;
import com.rmrf.statflux.repository.datasource.impl.SimpleDataSource;
import com.rmrf.statflux.repository.transaction.TransactionManager;

public class RepositoryConfig {
    private final DbConnectionConfig dbConnectionConfig = new DbConnectionConfigFromEnv();
    private final DbPooledConnectionConfig dbPooledConnectionConfig = new DbPooledConnectionConfigFromEnv();

    public DataSource simpleDataSource() {
        return new SimpleDataSource(dbConnectionConfig);
    }

    public DataSource pooledDataSource() {
        return new PooledDataSource(dbPooledConnectionConfig);
    }

    public TransactionManager transactionManager(DataSource dataSource) {
        return new TransactionManager(dataSource);
    }

    public LinkRepository linkRepository() {
        return new JdbcLinkRepository();
    }

    public PaginationStateRepository paginationStateRepository() {
        return new JdbcPaginationStateRepository();
    }

}
