package org.mercadolibre.camilo.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Seller {
    String id;
    String nickname;
    double reputation;

    Metrics metrics;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Metrics {
        double cancellations;
        double delays;
    }
}
