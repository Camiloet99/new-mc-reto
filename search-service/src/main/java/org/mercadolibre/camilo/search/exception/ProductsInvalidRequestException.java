package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class ProductsInvalidRequestException extends MiddlewareException {
    public ProductsInvalidRequestException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.UNPROCESSABLE_ENTITY.value(), ErrorCodes.PRODUCTS_INVALID_REQUEST,
                "Invalid request to products-service", uri, headers, responseBody);
    }
}
