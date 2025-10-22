package org.mercadolibre.camilo.qa.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.mercadolibre.camilo.qa.model.Question;

import java.util.List;

@Value
@Builder
public class QuestionResponse {
    String id;
    String productId;
    String author;
    String text;
    String createdAt;
    @Singular("answer")
    List<AnswerResponse> answers;

    public static QuestionResponse from(Question question) {
        List<AnswerResponse> ans = question.getAnswers() == null ? List.of()
                : question.getAnswers().stream().map(AnswerResponse::from).toList();

        return QuestionResponse.builder()
                .id(question.getId())
                .productId(question.getProductId())
                .author(question.getAuthor())
                .text(question.getText())
                .createdAt(question.getCreatedAt())
                .answers(ans)
                .build();
    }
}
