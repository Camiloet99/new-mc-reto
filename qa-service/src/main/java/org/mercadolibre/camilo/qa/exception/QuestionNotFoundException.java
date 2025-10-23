package org.mercadolibre.camilo.qa.exception;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(String id) {
        super("Question '%s' not found".formatted(id));
    }
}
