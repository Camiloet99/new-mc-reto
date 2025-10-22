package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;

public class ProductsUpstreamFailureException extends MiddlewareException {
    public ProductsUpstreamFailureException(int status, String uri, HttpHeaders headers, String responseBody) {
        super(status, ErrorCodes.PRODUCTS_UPSTREAM_FAILURE,
                "Unexpected response from products-service", uri, headers, responseBody);
    }
}
