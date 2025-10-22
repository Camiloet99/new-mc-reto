package com.mercadolibre.camilo.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Seller {
    String id;
    String nickname;
    double reputation;

    Metrics metrics;

    @Value
    @Builder
    public static class Metrics {
        double cancellations;
        double delays;
    }
}
