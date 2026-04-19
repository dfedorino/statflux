package com.rmrf.statflux.repository.datasource;

import com.rmrf.statflux.repository.exception.ConnectionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleDataSource implements DataSource {

    private final Connection connection;

    public SimpleDataSource(Properties props) {
        try {
            this.connection = DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
            );
        } catch (SQLException e) {
            log.error("SimpleDataSource[constructor] failed to get connection", e);
            throw new ConnectionException(e);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error("SimpleDataSource[close] failed to close connection", e);
            throw new ConnectionException(e);
        }
    }
}
