package org.mercadolibre.camilo.search.dto.enriched;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewResponse;

import java.util.List;

@Value
@Builder
@Schema(description = "Detalle enriquecido del producto con información extendida")
public class ItemEnrichedResponse {

    @Schema(description = "Detalle básico del producto")
    ItemBasicResponse basic;

    @Schema(description = "Información del vendedor asociado al producto")
    SellerResponse seller;

    @Schema(description = "Resumen de reseñas y calificaciones del producto")
    List<ReviewResponse> reviews;

    @Schema(description = "Resumen de reseñas y calificaciones del producto")
    @Singular("qaItem")
    List<QaResponse> qa;
}
