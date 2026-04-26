package com.rmrf.statflux.domain.exceptions;

/**
 * Исключение, представляющее попытку удаления ссылки по несуществующему идентификатору
 */
public class LinkIdNotFoundException extends RuntimeException {

    public LinkIdNotFoundException(String message) {
        super(message);
    }
}
