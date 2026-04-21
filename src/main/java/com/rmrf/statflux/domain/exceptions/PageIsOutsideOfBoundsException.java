package com.rmrf.statflux.domain.exceptions;

public class PageIsOutsideOfBoundsException extends RuntimeException {

    public PageIsOutsideOfBoundsException(String message) {
        super(message);
    }
}
