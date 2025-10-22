package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class QaInvalidRequestException extends MiddlewareException {
    public QaInvalidRequestException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ErrorCodes.QA_INVALID_REQUEST,
                "Invalid request to qa-service",
                uri, headers, responseBody);
    }
}
