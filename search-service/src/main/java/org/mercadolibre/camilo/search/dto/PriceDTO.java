package org.mercadolibre.camilo.search.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PriceDTO {
    BigDecimal amount;
    String currency;
}