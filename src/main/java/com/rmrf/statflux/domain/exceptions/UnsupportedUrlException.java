package com.rmrf.statflux.domain.exceptions;

/**
 * Исключение, представляющее попытку обработку (корректной) url неподдерживаемого хостинга видео
 */
public class UnsupportedUrlException extends RuntimeException {

    public UnsupportedUrlException(String message) {
        super(message);
    }
}
