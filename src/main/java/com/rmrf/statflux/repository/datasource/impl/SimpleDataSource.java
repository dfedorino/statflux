package com.rmrf.statflux.repository.datasource.impl;

import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.datasource.PerThreadConnectionHolder;
import com.rmrf.statflux.repository.exception.ConnectionException;
import com.rmrf.statflux.repository.util.Connections;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SimpleDataSource implements DataSource {

    private final Properties props;

    @Override
    public Connection getConnection() {
        if (PerThreadConnectionHolder.get() == null) {
            PerThreadConnectionHolder.set(Connections.initConnection(props));
        }
        return PerThreadConnectionHolder.get();
    }

    @Override
    public void close() {
        try {
            if (PerThreadConnectionHolder.get() != null) {
                getConnection().close();
            }
        } catch (SQLException e) {
            log.error("SimpleDataSource[close] failed to close connection", e);
            throw new ConnectionException(e);
        }
    }
}
