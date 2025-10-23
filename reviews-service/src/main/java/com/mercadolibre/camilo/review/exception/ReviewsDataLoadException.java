package com.mercadolibre.camilo.review.exception;

public class ReviewsDataLoadException extends RuntimeException {
    public ReviewsDataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReviewsDataLoadException(String message) {
        super(message);
    }
}
