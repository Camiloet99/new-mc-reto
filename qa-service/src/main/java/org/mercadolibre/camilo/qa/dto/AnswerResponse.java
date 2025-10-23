package org.mercadolibre.camilo.qa.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.mercadolibre.camilo.qa.model.Answer;

@Value
@Builder
@Schema(description = "Respuesta a una pregunta de producto")
public class AnswerResponse {

    @Schema(description = "Identificador único de la respuesta")
    String id;

    @Schema(description = "Identificador de la pregunta asociada")
    String questionId;

    @Schema(description = "Autor o usuario que respondió la pregunta")
    String author;

    @Schema(description = "Texto de la respuesta")
    String text;

    @Schema(description = "Fecha de creación de la respuesta en formato ISO-8601")
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
