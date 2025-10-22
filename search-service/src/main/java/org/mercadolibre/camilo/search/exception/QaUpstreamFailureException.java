package org.mercadolibre.camilo.search.exception;

import org.springframework.http.HttpHeaders;

public class QaUpstreamFailureException extends MiddlewareException {
    public QaUpstreamFailureException(int status, String uri, HttpHeaders headers, String responseBody) {
        super(status,
                ErrorCodes.QA_UPSTREAM_FAILURE,
                "Unexpected response from qa-service",
                uri, headers, responseBody);
    }
}
