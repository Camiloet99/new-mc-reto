package com.mercadolibre.camilo.review.dto;

import com.mercadolibre.camilo.review.model.Review;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewResponse {
    String id;
    String productId;
    int rating;
    String title;
    String text;
    String createdAt;
    String author;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId()).productId(review.getProductId()).rating(review.getRating())
                .title(review.getTitle()).text(review.getText()).createdAt(review.getCreatedAt())
                .author(review.getAuthor()).build();
    }
}
