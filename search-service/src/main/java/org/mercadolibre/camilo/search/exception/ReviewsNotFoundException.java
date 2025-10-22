package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class ReviewsNotFoundException extends MiddlewareException {
    public ReviewsNotFoundException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.NOT_FOUND.value(),
                ErrorCodes.REVIEWS_NOT_FOUND,
                "Reviews not found in reviews-service",
                uri, headers, responseBody);
    }
}
