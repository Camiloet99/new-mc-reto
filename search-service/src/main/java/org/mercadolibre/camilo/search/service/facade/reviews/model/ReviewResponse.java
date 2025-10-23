package org.mercadolibre.camilo.search.service.facade.reviews.model;

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
}