package org.mercadolibre.camilo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.mercadolibre.camilo.model.Seller;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "Información pública de un vendedor")
public class SellerResponse {

    @Schema(description = "ID único del vendedor")
    String id;

    @Schema(description = "Alias o nombre público del vendedor")
    String nickname;

    @Schema(description = "Reputación global del vendedor (0..1 o escala definida por negocio)")
    double reputation;

    @Schema(description = "Métricas operativas del vendedor")
    Metrics metrics;

    @Value
    @Builder
    @Schema(description = "Métricas operativas del vendedor (cancelaciones, demoras)")
    public static class Metrics {
        @Schema(description = "Proporción o tasa de cancelaciones en el período observado")
        double cancellations;

        @Schema(description = "Proporción o tasa de pedidos con demora")
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
