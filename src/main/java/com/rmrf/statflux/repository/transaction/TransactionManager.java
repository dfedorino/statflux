package com.rmrf.statflux.repository.transaction;

import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.exception.TransactionException;
import com.rmrf.statflux.repository.util.ConnectionUtils;
import java.sql.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TransactionManager {

    private final DataSource dataSource;

    public <T> T execute(TransactionCallback<T> callback) {
        Connection conn = null;
        Boolean oldAutoCommit = null;
        try {
            conn = dataSource.getConnection();
            oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T result = callback.doInTransaction();

            conn.commit();
            return result;

        } catch (Exception e) {
            if (conn != null) {
                ConnectionUtils.rollback(conn);
            }
            log.error("TransactionManager[execute] failed to execute callback", e);
            throw new TransactionException(e);
        } finally {
            if (oldAutoCommit != null) {
                ConnectionUtils.restoreOldAutoCommit(conn, oldAutoCommit);
            }
        }
    }
}
