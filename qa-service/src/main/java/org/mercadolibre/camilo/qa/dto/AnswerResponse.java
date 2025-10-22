package org.mercadolibre.camilo.qa.dto;

import lombok.Builder;
import lombok.Value;
import org.mercadolibre.camilo.qa.model.Answer;

@Value
@Builder
public class AnswerResponse {
    String id;
    String questionId;
    String author;
    String text;
    String createdAt;

    public static AnswerResponse from(Answer a) {
        return AnswerResponse.builder()
                .id(a.getId())
                .questionId(a.getQuestionId())
                .author(a.getAuthor())
                .text(a.getText())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
