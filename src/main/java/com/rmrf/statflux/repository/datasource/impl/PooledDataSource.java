package com.rmrf.statflux.repository.datasource.impl;

import com.rmrf.statflux.repository.config.DbPooledConnectionConfig;
import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.exception.ConnectionException;
import com.rmrf.statflux.repository.util.Connections;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PooledDataSource implements DataSource {

    private final BlockingQueue<Connection> pool;
    private final List<Connection> allConnections;

    public PooledDataSource(DbPooledConnectionConfig connectionConfig) {
        int poolSize = connectionConfig.getPoolSize();
        this.pool = new ArrayBlockingQueue<>(poolSize);
        this.allConnections = new ArrayList<>(poolSize);
        log.info("initializing connection pool if size {}",  poolSize);
        for (int i = 0; i < poolSize; i++) {
            Connection conn = Connections.initConnection(connectionConfig);
            pool.offer(wrap(conn));
            allConnections.add(conn);
        }
        log.info("connection pool initialized");
    }

    @Override
    public Connection getConnection() {
        try {
            log.debug("PooledDataSource[getConnection] connection request");
            return pool.take(); // blocks when empty
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConnectionException(e);
        }
    }

    /**
     * Оборачивает соединение {@link Connection} в прокси, перехватывающий вызов {@code close()}.
     * Вместо фактического закрытия соединения вызов {@code close()} у возвращаемого прокси
     * возвращает соединение обратно в пул.
     * Остальные методы делегируются соединению.
     *
     * @param realConn фактическое JDBC-соединение
     * @return прокси {@link Connection}, возвращающий себя в пул при вызове {@code close()}
     */
    private Connection wrap(Connection realConn) {
        return (Connection) Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    pool.put((Connection) proxy); // return to pool
                    return null;
                }
                return method.invoke(realConn, args);
            }
        );
    }

    @Override
    public void close() {
        for (Connection conn : allConnections) {
            try {
                conn.close(); // real close
            } catch (SQLException e) {
                log.error("Error closing connection", e);
            }
        }
    }
}
