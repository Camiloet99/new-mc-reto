package org.mercadolibre.camilo.search.exception;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class CategoriesInvalidRequestException extends MiddlewareException {
    public CategoriesInvalidRequestException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ErrorCodes.CATEGORIES_INVALID_REQUEST,
                "Invalid request to categories-service",
                uri, headers, responseBody);
    }
}