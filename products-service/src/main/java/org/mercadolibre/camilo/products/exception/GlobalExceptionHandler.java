package org.mercadolibre.camilo.products.exception;

import org.mercadolibre.camilo.products.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> onInvalid(InvalidRequestException ex) {
        log.warn("Invalid request: {}", ex.getMessage());

        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCodes.INVALID_REQUEST)
                .description(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> onNotFound(ProductNotFoundException ex) {
        log.warn("Product not found: {}", ex.getMessage());
        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCodes.PRODUCT_NOT_FOUND)
                .description(ex.getMessage())
                .httpStatus(HttpStatus.NOT_FOUND.value())
                .build();
        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> onConstraint(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCodes.INVALID_REQUEST)
                .description(details.isBlank() ? "Constraint violation" : details)
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> onMethodArgInvalid(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCodes.INVALID_REQUEST)
                .description(details.isBlank() ? "Invalid request body" : details)
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<org.springframework.http.ResponseEntity<ErrorResponse>> onAny(Throwable ex) {
        log.error("Unexpected error", ex);
        ErrorResponse body = ErrorResponse.builder()
                .code(ErrorCodes.UNKNOWN_ERROR)
                .description(ex.getMessage() == null ? "Unexpected error" : ex.getMessage())
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return Mono.just(org.springframework.http.ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body));
    }
}