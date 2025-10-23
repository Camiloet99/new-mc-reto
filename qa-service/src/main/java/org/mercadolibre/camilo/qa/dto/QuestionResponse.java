package org.mercadolibre.camilo.qa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.mercadolibre.camilo.qa.model.Question;

import java.util.List;

@Value
@Builder
@Schema(description = "Pregunta realizada sobre un producto, incluyendo sus respuestas")
public class QuestionResponse {

    @Schema(description = "Identificador único de la pregunta")
    String id;

    @Schema(description = "Identificador del producto al que pertenece la pregunta")
    String productId;

    @Schema(description = "Autor o usuario que realizó la pregunta")
    String author;

    @Schema(description = "Texto de la pregunta")
    String text;

    @Schema(description = "Fecha de creación de la pregunta en formato ISO-8601")
    String createdAt;

    @Singular("answer")
    @Schema(description = "Lista de respuestas asociadas a la pregunta")
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
