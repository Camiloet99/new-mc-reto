package com.mercadolibre.camilo.products.exception;

public class ProductsDataLoadException extends RuntimeException {
    public ProductsDataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}