package org.mercadolibre.camilo.qa.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Question {
    String id;
    String productId;
    String author;
    String text;
    String createdAt; // ISO-8601
    @Singular("answer")
    List<Answer> answers; // incrustadas en el JSON
}
