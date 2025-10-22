package org.mercadolibre.camilo.search.exception;

import org.mercadolibre.camilo.search.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MiddlewareException.class)
    public Mono<ResponseEntity<ErrorResponse>> onMiddleware(MiddlewareException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .code(ex.getErrorCode())
                .description(ex.getDescription())
                .httpStatus(ex.getHttpStatus())
                .uri(ex.getUri())
                .headers(ex.getHeaders() == null ? null : ex.getHeaders().toSingleValueMap())
                .responseBody(ex.getResponseBody())
                .build();

        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> onRSE(ResponseStatusException ex) {
        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCodes.UNKNOWN_ERROR)
                .description(ex.getReason())
                .httpStatus(ex.getStatusCode().value())
                .build();
        return Mono.just(ResponseEntity
                .status(ex.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<ErrorResponse>> onAny(Throwable ex) {
        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCodes.UNKNOWN_ERROR)
                .description(ex.getMessage())
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }
}