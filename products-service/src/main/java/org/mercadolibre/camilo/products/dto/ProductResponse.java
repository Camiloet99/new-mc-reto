package com.mercadolibre.camilo.products.dto;

import com.mercadolibre.camilo.products.model.Attribute;
import com.mercadolibre.camilo.products.model.Product;
import lombok.Builder;
import lombok.Singular;
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
    @Singular("attr")
    List<Attribute> attributes;
    String condition;
    String description;
    Integer stock;
    Boolean hasPromotion;

    public static ProductResponse from(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .price(p.getPrice())
                .currency(p.getCurrency())
                .thumbnail(p.getThumbnail())
                .pictures(p.getPictures())
                .sellerId(p.getSellerId())
                .categoryId(p.getCategoryId())
                .attributes(p.getAttributes())
                .condition(p.getCondition())
                .description(p.getDescription())
                .stock(p.getStock())
                .hasPromotion(p.getHasPromotion())
                .build();
    }
}
