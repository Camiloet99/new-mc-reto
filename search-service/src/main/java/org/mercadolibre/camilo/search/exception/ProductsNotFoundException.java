package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class ProductsNotFoundException extends MiddlewareException {
    public ProductsNotFoundException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.NOT_FOUND.value(), ErrorCodes.PRODUCTS_NOT_FOUND,
                "Product not found in products-service", uri, headers, responseBody);
    }
}
