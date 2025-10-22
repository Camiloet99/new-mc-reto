package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;

public class CategoriesUpstreamFailureException extends MiddlewareException {
    public CategoriesUpstreamFailureException(int status, String uri, HttpHeaders headers, String responseBody) {
        super(status,
                ErrorCodes.CATEGORIES_UPSTREAM_FAILURE,
                "Unexpected response from categories-service",
                uri, headers, responseBody);
    }
}
