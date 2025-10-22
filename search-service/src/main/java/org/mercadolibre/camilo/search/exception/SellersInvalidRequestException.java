package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class SellersInvalidRequestException extends MiddlewareException {
    public SellersInvalidRequestException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ErrorCodes.SELLERS_INVALID_REQUEST,
                "Invalid request to sellers-service",
                uri, headers, responseBody);
    }
}