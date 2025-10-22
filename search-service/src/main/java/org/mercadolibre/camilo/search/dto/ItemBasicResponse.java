package org.mercadolibre.camilo.search.dto;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;

import java.util.List;

@Value
@Builder
public class ItemBasicResponse {
    String id;
    String title;
    PriceDTO price;
    String thumbnail;
    @Singular("picture")
    List<String> pictures;
    String condition;
    Integer stock;
    Boolean hasPromotion;

    @Singular("attribute") List<ProductResponse.Attribute> attributes;
    List<CategoryResponse.BreadcrumbNode> categoryBreadcrumb;

    public static ItemBasicResponse from(ProductResponse p, List<CategoryResponse.BreadcrumbNode> breadcrumb) {
        return ItemBasicResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .price(PriceDTO.builder().amount(p.getPrice()).currency(p.getCurrency()).build())
                .thumbnail(p.getThumbnail())
                .pictures(p.getPictures())
                .condition(p.getCondition())
                .stock(p.getStock())
                .hasPromotion(p.getHasPromotion())
                .attributes(p.getAttributes())
                .categoryBreadcrumb(breadcrumb)
                .build();
    }
}
