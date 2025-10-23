package org.mercadolibre.camilo.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
@Schema(description = "Respuesta paginada genérica")
public class PageResponse<T> {
    @Schema(description = "Página actual (base 0)", example = "0")
    int page;

    @Schema(description = "Tamaño de página (elementos por página)", example = "5")
    int size;

    @Schema(description = "Total de elementos", example = "123")
    long totalItems;

    @Schema(description = "Total de páginas", example = "25")
    int totalPages;

    @Schema(description = "¿Existe página anterior?", example = "false")
    boolean hasPrev;

    @Schema(description = "¿Existe página siguiente?", example = "true")
    boolean hasNext;

    @Singular
    @Schema(description = "Elementos de la página")
    List<T> items;
}