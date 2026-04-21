package com.rmrf.statflux.repository.util;

import com.rmrf.statflux.repository.config.DbConnectionConfig;
import com.rmrf.statflux.repository.exception.ConnectionException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Connections {

    public static Connection initConnection(DbConnectionConfig connectionConfig) {
        try {
            return DriverManager.getConnection(
                connectionConfig.getUrl(),
                connectionConfig.getUsername(),
                connectionConfig.getPassword()
            );
        } catch (SQLException e) {
            log.error("ConnectionUtils[initConnection] failed to init connection", e);
            throw new ConnectionException("Failed to init connection", e);
        }
    }

    public static void closeQuietly(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("ConnectionUtils[closeQuietly] Failed to close connection", e);
            }
        }
    }

    public static void restoreOldAutoCommit(
        @NonNull Connection conn,
        @NonNull Boolean oldAutoCommit
    ) {
        try {
            conn.setAutoCommit(oldAutoCommit);
        } catch (SQLException e) {
            log.error("ConnectionUtils[restoreOldAutoCommit] failed to execute callback", e);
            throw new ConnectionException("Failed to restore old autocommit", e);
        }
    }

    public static void rollback(@NonNull Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.error("ConnectionUtils[rollback] failed to rollback", e);
            throw new ConnectionException("Rollback failed", e);
        }
    }

}
