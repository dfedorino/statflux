package com.rmrf.statflux.repository.exception;

import java.util.Arrays;

public class QueryException extends RuntimeException {

    public QueryException(String sql, Object[] params, Throwable cause) {
        super("Query failed: " + sql + ", params=" + Arrays.toString(params), cause);
    }
}
