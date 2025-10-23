package com.mercadolibre.camilo.exceptions;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) { super(message); }
}