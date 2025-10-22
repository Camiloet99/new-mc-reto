package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class CategoriesNotFoundException extends MiddlewareException {
    public CategoriesNotFoundException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.NOT_FOUND.value(),
                ErrorCodes.CATEGORIES_NOT_FOUND,
                "Category not found in categories-service",
                uri, headers, responseBody);
    }
}