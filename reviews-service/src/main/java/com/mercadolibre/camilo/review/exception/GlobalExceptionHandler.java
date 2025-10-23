package com.mercadolibre.camilo.review.exception;

import com.mercadolibre.camilo.review.model.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public Mono<ResponseEntity<ErrorResponse>> onInvalid(InvalidRequestException ex) {
        log.warn("Invalid request: {}", ex.getMessage());
        var body = ErrorResponse.builder()
                .code(ErrorCodes.INVALID_REQUEST)
                .description(ex.getMessage())
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(body));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Mono<ResponseEntity<ErrorResponse>> onConstraint(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        var body = ErrorResponse.builder()
                .code(ErrorCodes.INVALID_REQUEST)
                .description(details.isBlank() ? "Constraint violation" : details)
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ErrorResponse>> onMethodArgInvalid(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + (fe.getDefaultMessage() == null ? "invalid" : fe.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        var body = ErrorResponse.builder()
                .code(ErrorCodes.INVALID_REQUEST)
                .description(details.isBlank() ? "Invalid request body" : details)
                .httpStatus(HttpStatus.BAD_REQUEST.value())
                .build();
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(body));
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<ErrorResponse>> onAny(Throwable ex) {
        log.error("Unexpected error", ex);
        var body = ErrorResponse.builder()
                .code(ErrorCodes.UNKNOWN_ERROR)
                .description(ex.getMessage() == null ? "Unexpected error" : ex.getMessage())
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON).body(body));
    }
}