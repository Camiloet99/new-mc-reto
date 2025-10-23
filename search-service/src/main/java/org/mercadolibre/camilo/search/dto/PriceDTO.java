package org.mercadolibre.camilo.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@Schema(description = "Objeto de precio del producto")
public class PriceDTO {

    @Schema(description = "Monto del precio")
    BigDecimal amount;

    @Schema(description = "Moneda del precio (por ejemplo USD, COP, EUR)")
    String currency;
}