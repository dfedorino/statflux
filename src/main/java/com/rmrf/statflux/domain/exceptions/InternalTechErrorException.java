package com.rmrf.statflux.domain.exceptions;

/**
 * Исключение, представляющее техническую ошибку
 */
public class InternalTechErrorException extends RuntimeException {

    public InternalTechErrorException(String message) {
        super(message);
    }
}
