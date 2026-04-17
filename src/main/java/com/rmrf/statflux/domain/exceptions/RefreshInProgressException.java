package com.rmrf.statflux.domain.exceptions;

/**
 * Исключение, представляющее попытку запустить процесс обновление метаданных при наличии активного
 * процесса обновления метаданных
 */
public class RefreshInProgressException extends RuntimeException {

    public RefreshInProgressException(String message) {
        super(message);
    }
}
