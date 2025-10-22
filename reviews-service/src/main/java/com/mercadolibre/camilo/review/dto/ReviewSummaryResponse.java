package com.mercadolibre.camilo.review.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ReviewSummaryResponse {
    String productId;
    long count;
    double avg;                   // promedio simple
    @Singular("bin")
    Map<Integer, Long> histogram; // rating -> count
}
