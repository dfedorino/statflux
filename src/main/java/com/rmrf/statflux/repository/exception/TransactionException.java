package com.rmrf.statflux.repository.exception;

public class TransactionException extends RuntimeException {

    public TransactionException(Exception e) {
        super(e);
    }

}
