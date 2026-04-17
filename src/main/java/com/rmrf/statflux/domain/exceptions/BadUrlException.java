package com.rmrf.statflux.domain.exceptions;

/**
 * Исключение, представляющее техническую ошибку парсинга URL
 */
public class BadUrlException extends RuntimeException {

    public BadUrlException(String message) {
        super(message);
    }
}
