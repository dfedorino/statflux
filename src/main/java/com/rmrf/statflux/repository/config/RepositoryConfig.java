package com.rmrf.statflux.repository.config;

import com.rmrf.statflux.common.ConfigLoader;
import com.rmrf.statflux.repository.JdbcLinkRepository;
import com.rmrf.statflux.repository.JdbcPaginationStateRepository;
import com.rmrf.statflux.repository.LinkRepository;
import com.rmrf.statflux.repository.PaginationStateRepository;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.datasource.SimpleDataSource;
import com.rmrf.statflux.repository.query.QueryExecutor;
import com.rmrf.statflux.repository.query.SqlScriptRunner;

public class RepositoryConfig {

    public DataSource dataSource() {
        return new SimpleDataSource(ConfigLoader.load("application.properties"));
    }

    public QueryExecutor queryExecutor() {
        return new QueryExecutor(dataSource());
    }

    public SqlScriptRunner sqlScriptRunner() {
        return new SqlScriptRunner(queryExecutor());
    }

    public LinkRepository linkRepository() {
        return new JdbcLinkRepository(queryExecutor());
    }

    public PaginationStateRepository paginationStateRepository() {
        return new JdbcPaginationStateRepository(queryExecutor());
    }

}
