package com.mercadolibre.camilo.category.exception;

public class CategoriesDataLoadException extends RuntimeException {
    public CategoriesDataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public CategoriesDataLoadException(String message) {
        super(message);
    }
}