package org.mercadolibre.camilo.search.service.facade.qa.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class QaResponse {
    String id;
    String productId;
    String author;
    String text;
    String createdAt;
    List<AnswerDTO> answers;

    @Value
    @Builder
    public static class AnswerDTO {
        String id;
        String questionId;
        String author;
        String text;
        String createdAt;
    }
}
