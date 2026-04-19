package com.rmrf.statflux.repository.config;

import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.repository.JdbcLinkRepository;
import com.rmrf.statflux.repository.JdbcPaginationStateRepository;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.datasource.impl.PooledDataSource;
import com.rmrf.statflux.repository.datasource.impl.SimpleDataSource;
import com.rmrf.statflux.repository.transaction.TransactionManager;

public class RepositoryConfig {

    public DataSource simpleDataSource() {
        return new SimpleDataSource(ConfigLoader.loadProperties("application.properties"));
    }

    public DataSource pooledDataSource() {
        return new PooledDataSource(ConfigLoader.loadProperties("application.properties"));
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
