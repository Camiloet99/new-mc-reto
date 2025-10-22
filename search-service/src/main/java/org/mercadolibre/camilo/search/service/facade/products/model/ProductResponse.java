package org.mercadolibre.camilo.search.service.facade.products.model;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
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
    public static class Attribute {
        String name;
        String value;
    }
}
