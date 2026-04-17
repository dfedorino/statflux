package com.rmrf.statflux.repository.exception;

public class ConnectionException extends RuntimeException {

    public ConnectionException(Exception e) {
        super(e);
    }

    public ConnectionException(String message, Exception e) {
        super(message, e);
    }
}
