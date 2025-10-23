package org.mercadolibre.camilo.review.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Review {
    String id;
    String productId;
    int rating;         // 1..5
    String title;
    String text;
    String createdAt;   // ISO-8601 string (simple en MVP)
    String author;      // nickname
}