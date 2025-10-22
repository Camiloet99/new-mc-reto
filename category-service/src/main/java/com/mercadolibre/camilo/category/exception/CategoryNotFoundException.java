package com.mercadolibre.camilo.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String id) {
        super("Category '%s' not found".formatted(id));
    }
}