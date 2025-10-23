package org.mercadolibre.camilo.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;

import java.util.List;

@Value
@Builder
public class ItemBasicResponse {

    @Schema(description = "ID único del producto")
    String id;

    @Schema(description = "Título o nombre del producto")
    String title;

    @Schema(description = "Información de precio del producto")
    PriceDTO price;

    @Schema(description = "URL de la miniatura principal del producto")
    String thumbnail;

    @Schema(description = "Lista de URLs de imágenes adicionales")
    @Singular("picture")
    List<String> pictures;

    @Schema(description = "Condición del producto (nuevo, usado, reacondicionado, etc.)")
    String condition;

    @Schema(description = "Cantidad de unidades disponibles en inventario")
    Integer stock;

    @Schema(description = "Indica si el producto tiene promoción activa")
    Boolean hasPromotion;


    @Schema(description = "Atributos descriptivos del producto (color, tamaño, material, etc.)")
    @Singular("attribute")
    List<ProductResponse.Attribute> attributes;

    @Schema(description = "Ruta jerárquica de categorías (breadcrumb)")
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
