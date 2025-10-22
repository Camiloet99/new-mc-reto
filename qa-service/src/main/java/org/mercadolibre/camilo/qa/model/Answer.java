package org.mercadolibre.camilo.qa.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Answer {
    String id;
    String questionId;
    String author;
    String text;
    String createdAt; // ISO-8601
}
