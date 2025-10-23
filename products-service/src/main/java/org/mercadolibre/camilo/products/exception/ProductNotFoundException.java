package com.mercadolibre.camilo.products.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String id) {
        super("Product '%s' not found".formatted(id));
    }
}
