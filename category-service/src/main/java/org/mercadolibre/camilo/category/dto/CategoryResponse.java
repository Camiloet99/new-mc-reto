package org.mercadolibre.camilo.category.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "Respuesta de categoría con campos derivados")
public class CategoryResponse {

    @Schema(example = "MLA1051")
    String id;

    @Schema(example = "Celulares y Teléfonos")
    String name;

    @Schema(description = "ID del padre (puede ser null en raíz)", example = "MLA1055", nullable = true)
    String parentId;

    @Singular("crumb")
    @Schema(description = "Ruta desde la raíz (incluye raíz y la categoría)")
    List<BreadcrumbNode> pathFromRoot;

    @Schema(description = "Cantidad de hijos directos", example = "24")
    int childrenCount;
}
