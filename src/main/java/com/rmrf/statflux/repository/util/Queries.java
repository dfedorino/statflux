package com.rmrf.statflux.repository.util;

import com.rmrf.statflux.repository.datasource.PerThreadConnectionHolder;
import com.rmrf.statflux.repository.exception.ConnectionException;
import com.rmrf.statflux.repository.exception.QueryException;
import com.rmrf.statflux.repository.query.ResultSetMapper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class Queries {

    public <T> List<T> query(String sql, ResultSetMapper<T> mapper, Object... params) {
        Connection conn = getConnection();
        try (PreparedStatement stmt = prepare(conn, sql, params);
            ResultSet rs = stmt.executeQuery()
        ) {
            log.debug("executing query {} using connection {} ", sql, conn);
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapper.map(rs));
            }
            return result;
        } catch (SQLException e) {
            log.error("QueryExecutor[query] failed to run query", e);
            throw new QueryException(sql, params, e);
        }
    }

    public int update(String sql, Object... params) {
        Connection conn = getConnection();
        try (PreparedStatement stmt = prepare(conn, sql, params)
        ) {
            log.debug("executing update {} using connection {} ", sql, conn);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("QueryExecutor[update] failed to run update", e);
            throw new QueryException(sql, params, e);
        }
    }

    private PreparedStatement prepare(Connection conn, String sql, Object... params) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }

    private Connection getConnection() {
        Connection txConn = PerThreadConnectionHolder.get();
        if (txConn == null) {
            throw new ConnectionException("Queries[getConnection] connection is not initialized");
        }
        return txConn;
    }
}
