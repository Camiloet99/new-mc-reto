package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class SellersNotFoundException extends MiddlewareException {
    public SellersNotFoundException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.NOT_FOUND.value(),
                ErrorCodes.SELLERS_NOT_FOUND,
                "Seller not found in sellers-service",
                uri, headers, responseBody);
    }
}
