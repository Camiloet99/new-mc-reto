package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;

public class SellersUpstreamFailureException extends MiddlewareException {
    public SellersUpstreamFailureException(int status, String uri, HttpHeaders headers, String responseBody) {
        super(status,
                ErrorCodes.SELLERS_UPSTREAM_FAILURE,
                "Unexpected response from sellers-service",
                uri, headers, responseBody);
    }
}
