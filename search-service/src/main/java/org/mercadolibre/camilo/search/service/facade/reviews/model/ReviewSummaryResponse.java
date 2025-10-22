package org.mercadolibre.camilo.search.service.facade.reviews.model;

import lombok.Value;

import java.util.Map;

@Value
public class ReviewSummaryResponse {
    String productId;
    long count;
    double avg;
    Map<Integer, Long> histogram;
}