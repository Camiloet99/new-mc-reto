package org.mercadolibre.camilo.review.dto;

import org.mercadolibre.camilo.review.model.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewResponse {

    @Schema(description = "ID único de la reseña")
    String id;

    @Schema(description = "ID del producto al que pertenece la reseña")
    String productId;

    @Schema(description = "Calificación otorgada (1 a 5)")
    int rating;

    @Schema(description = "Título de la reseña")
    String title;

    @Schema(description = "Texto descriptivo de la reseña")
    String text;

    @Schema(description = "Fecha de creación en formato ISO-8601")
    String createdAt;

    @Schema(description = "Nombre o alias del autor de la reseña")
    String author;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId()).productId(review.getProductId()).rating(review.getRating())
                .title(review.getTitle()).text(review.getText()).createdAt(review.getCreatedAt())
                .author(review.getAuthor()).build();
    }
}
