package org.mercadolibre.camilo.products.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.mercadolibre.camilo.products.model.Attribute;
import org.mercadolibre.camilo.products.model.Product;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
@Builder
@Schema(description = "Representación de un producto en el catálogo")
public class ProductResponse {

    @Schema(description = "ID único del producto")
    String id;

    @Schema(description = "Título o nombre comercial del producto")
    String title;

    @Schema(description = "Precio del producto")
    BigDecimal price;

    @Schema(description = "Moneda en la que está expresado el precio (por ejemplo USD, COP, EUR)")
    String currency;

    @Schema(description = "URL de la miniatura principal del producto")
    String thumbnail;

    @Schema(description = "Lista de URLs de imágenes adicionales")
    List<String> pictures;

    @Schema(description = "Identificador del vendedor asociado")
    String sellerId;

    @Schema(description = "Identificador de la categoría a la que pertenece el producto")
    String categoryId;

    @Singular("attr")
    @Schema(description = "Lista de atributos descriptivos del producto (color, tamaño, material, etc.)")
    List<Attribute> attributes;

    @Schema(description = "Condición del producto (nuevo, usado, reacondicionado, etc.)")
    String condition;

    @Schema(description = "Descripción detallada del producto")
    String description;

    @Schema(description = "Cantidad de unidades disponibles en inventario")
    Integer stock;

    @Schema(description = "Indica si el producto tiene una promoción activa")
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
