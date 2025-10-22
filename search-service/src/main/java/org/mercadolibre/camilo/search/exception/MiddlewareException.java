package org.mercadolibre.camilo.search.exception;

import lombok.Getter;
import org.springframework.http.HttpHeaders;

@Getter
public class MiddlewareException extends RuntimeException {

    private final int httpStatus;
    private final String errorCode;
    private final String description;
    private final String uri;
    private final HttpHeaders headers;
    private final String responseBody;

    public MiddlewareException(int httpStatus, String errorCode, String description,
                               String uri, HttpHeaders headers, String responseBody, Throwable cause) {
        super(description, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.description = description;
        this.uri = uri;
        this.headers = headers;
        this.responseBody = responseBody;
    }

    public MiddlewareException(int httpStatus, String errorCode, String description,
                               String uri, HttpHeaders headers, String responseBody) {
        this(httpStatus, errorCode, description, uri, headers, responseBody, null);
    }
}