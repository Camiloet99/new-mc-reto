package com.mercadolibre.camilo.exceptions;

public class SellerNotFoundException extends RuntimeException {
    public SellerNotFoundException(String id) {
        super("Seller '%s' not found".formatted(id));
    }
}
