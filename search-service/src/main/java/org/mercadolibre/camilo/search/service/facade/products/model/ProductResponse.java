package org.mercadolibre.camilo.search.service.facade.products.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
public class ProductResponse {
    String id;
    String title;
    BigDecimal price;
    String currency;
    String thumbnail;
    List<String> pictures;
    String sellerId;
    String categoryId;
    List<Attribute> attributes;
    String condition;
    String description;
    Integer stock;
    Boolean hasPromotion;

    @Value
    @Builder
    public static class Attribute {
        String name;
        String value;
    }
}
