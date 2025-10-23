package org.mercadolibre.camilo.search.service.facade.seller.model;

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
    public static class Metrics {
        double cancellations;
        double delays;
    }
}
