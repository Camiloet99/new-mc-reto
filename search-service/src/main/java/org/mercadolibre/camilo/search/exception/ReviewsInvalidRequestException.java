package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class ReviewsInvalidRequestException extends MiddlewareException {
    public ReviewsInvalidRequestException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ErrorCodes.REVIEWS_INVALID_REQUEST,
                "Invalid request to reviews-service",
                uri, headers, responseBody);
    }
}
