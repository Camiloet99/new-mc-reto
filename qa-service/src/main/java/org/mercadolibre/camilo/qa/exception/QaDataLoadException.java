package org.mercadolibre.camilo.qa.exception;

public class QaDataLoadException extends RuntimeException {
    public QaDataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public QaDataLoadException(String message) {
        super(message);
    }
}
