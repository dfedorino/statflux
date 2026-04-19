package com.rmrf.statflux.repository.exception;

public class ConnectionException extends RuntimeException {

    public ConnectionException(Exception e) {
        this(null, e);
    }

    public ConnectionException(String message) {
        this(message, null);
    }

    public ConnectionException(String message, Exception e) {
        super(message, e);
    }
}
