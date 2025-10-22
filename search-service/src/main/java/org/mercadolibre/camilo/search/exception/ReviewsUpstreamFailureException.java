package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;

public class ReviewsUpstreamFailureException extends MiddlewareException {
    public ReviewsUpstreamFailureException(int status, String uri, HttpHeaders headers, String responseBody) {
        super(status,
                ErrorCodes.REVIEWS_UPSTREAM_FAILURE,
                "Unexpected response from reviews-service",
                uri, headers, responseBody);
    }
}
