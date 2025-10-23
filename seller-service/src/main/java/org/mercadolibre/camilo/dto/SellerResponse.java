package com.mercadolibre.camilo.dto;

import com.mercadolibre.camilo.model.Seller;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SellerResponse {
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

    public static SellerResponse from(Seller s) {
        return SellerResponse.builder()
                .id(s.getId())
                .nickname(s.getNickname())
                .reputation(s.getReputation())
                .metrics(Metrics.builder()
                        .cancellations(s.getMetrics().getCancellations())
                        .delays(s.getMetrics().getDelays())
                        .build())
                .build();
    }
}
