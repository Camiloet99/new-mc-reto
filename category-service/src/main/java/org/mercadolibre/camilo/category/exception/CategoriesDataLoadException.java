package org.mercadolibre.camilo.category.exception;

public class CategoriesDataLoadException extends RuntimeException {
    public CategoriesDataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}