package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class QaNotFoundException extends MiddlewareException {
    public QaNotFoundException(String uri, HttpHeaders headers, String responseBody) {
        super(HttpStatus.NOT_FOUND.value(),
                ErrorCodes.QA_NOT_FOUND,
                "Q&A not found in qa-service",
                uri, headers, responseBody);
    }
}
