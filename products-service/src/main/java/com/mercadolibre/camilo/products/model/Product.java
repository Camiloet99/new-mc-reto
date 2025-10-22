package com.mercadolibre.camilo.products.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    String id;
    String title;
    BigDecimal price;
    String currency;
    String thumbnail;            // principal
    List<String> pictures;       // nuevas: URLs adicionales
    String sellerId;
    String categoryId;
    List<Attribute> attributes;  // características
    String condition;            // "NEW" | "USED" (string simple por ahora)
    String description;          // descripción larga simple
    Integer stock;               // en MVP lo mantenemos aquí
    Boolean hasPromotion;        // en MVP (futuro: promotions-service)
}
