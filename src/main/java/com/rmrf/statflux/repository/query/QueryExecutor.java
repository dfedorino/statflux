package com.rmrf.statflux.repository.query;

import com.rmrf.statflux.repository.datasource.DataSource;
import com.rmrf.statflux.repository.exception.QueryException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class QueryExecutor {

    private final DataSource dataSource;

    public <T> List<T> query(String sql, ResultSetMapper<T> mapper, Object... params) {
        try (PreparedStatement stmt = prepare(sql, params);
            ResultSet rs = stmt.executeQuery()) {

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
        try (PreparedStatement stmt = prepare(sql, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            log.error("QueryExecutor[update] failed to run update", e);
            throw new QueryException(sql, params, e);
        }
    }

    private PreparedStatement prepare(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = dataSource.getConnection().prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }
}
