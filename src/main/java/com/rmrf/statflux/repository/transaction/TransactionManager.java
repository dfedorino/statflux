package com.rmrf.statflux.repository.transaction;

import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.datasource.PerThreadConnectionHolder;
import com.rmrf.statflux.repository.exception.TransactionException;
import com.rmrf.statflux.repository.util.Connections;
import java.sql.Connection;
import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TransactionManager {

    private final DataSource dataSource;

    public <T> T execute(Callable<T> callback) {
        Connection conn = null;
        Boolean oldAutoCommit = null;

        try {
            conn = dataSource.getConnection();
            PerThreadConnectionHolder.set(conn);

            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T result = callback.call();

            conn.commit();
            return result;

        } catch (Exception e) {
            if (conn != null) {
                Connections.rollback(conn);
            }
            throw new TransactionException(e);

        } finally {
            if (oldAutoCommit != null) {
                Connections.restoreOldAutoCommit(conn, oldAutoCommit);
            }
            PerThreadConnectionHolder.clear();
            Connections.closeQuietly(conn);
        }
    }

    public void executeWithoutResult(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }
}
