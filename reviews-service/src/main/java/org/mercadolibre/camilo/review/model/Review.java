package org.mercadolibre.camilo.review.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Review {
    String id;
    String productId;
    int rating;
    String title;
    String text;
    String createdAt;
    String author;
}