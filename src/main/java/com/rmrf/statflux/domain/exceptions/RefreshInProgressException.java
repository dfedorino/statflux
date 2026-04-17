package com.rmrf.statflux.domain.exceptions;

public class RefreshInProgressException extends RuntimeException {

    public RefreshInProgressException(String message) {
        super(message);
    }
}
