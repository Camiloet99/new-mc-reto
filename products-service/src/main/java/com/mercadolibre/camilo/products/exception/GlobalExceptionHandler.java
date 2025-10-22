package com.mercadolibre.camilo.products.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseStatusException> handleNotFound(NotFoundException ex) {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage()));
    }
}
