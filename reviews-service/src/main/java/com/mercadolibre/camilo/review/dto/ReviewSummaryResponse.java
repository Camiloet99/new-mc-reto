package com.mercadolibre.camilo.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ReviewSummaryResponse {

    @Schema(description = "ID del producto")
    String productId;

    @Schema(description = "Cantidad total de reseñas recibidas")
    long count;

    @Schema(description = "Promedio simple de las calificaciones")
    double avg;

    @Singular("bin")
    @Schema(description = "Histograma 1..5 (clave=rating, valor=cantidad)")
    Map<Integer, Long> histogram;
}
