package com.rmrf.statflux.repository.exception;

public class SqlScriptException extends RuntimeException {

    public SqlScriptException(String message) {
        super(message);
    }

    public SqlScriptException(String message, Throwable cause) {
        super(message, cause);
    }

}
