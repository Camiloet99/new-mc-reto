package org.mercadolibre.camilo.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Schema(description = "Nodo del breadcrumb")
@Value
@Builder
public class BreadcrumbNode {
    @Schema(description = "ID de la categoría", example = "MLA1051")
    String id;

    @Schema(description = "Nombre de la categoría", example = "Celulares y Teléfonos")
    String name;
}
