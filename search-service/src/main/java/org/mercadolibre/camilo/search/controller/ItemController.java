package org.mercadolibre.camilo.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.PageResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import org.mercadolibre.camilo.search.model.ErrorResponse;
import org.mercadolibre.camilo.search.service.impl.ItemServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Tag(name = "Items", description = "Operaciones relacionadas con los productos")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
public class ItemController {

    private final ItemServiceImpl service;

    @Operation(summary = "Obtiene el detalle básico del producto",
            description = "Devuelve información esencial del producto: título, precio, imágenes, condición, stock, atributos y breadcrumb de categoría.")
    @ApiResponse(responseCode = "200", description = "Detalle básico del producto encontrado",
            content = @Content(schema = @Schema(implementation = ItemBasicResponse.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ItemBasicResponse>> getBasic(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable String id) {
        return service.basic(id)
                .map(body -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body));
    }

    @Operation(summary = "Obtiene el detalle enriquecido del producto",
            description = "Devuelve información extendida del producto, combinando datos básicos con reseñas, categorías, vendedores u otros metadatos.")
    @ApiResponse(responseCode = "200", description = "Detalle enriquecido del producto encontrado",
            content = @Content(schema = @Schema(implementation = ItemEnrichedResponse.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}/enriched")
    public Mono<ResponseEntity<ItemEnrichedResponse>> getEnriched(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable String id) {
        return service.enriched(id)
                .map(body -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body));
    }

    @Operation(summary = "Lista productos enriquecidos con filtros y paginación", description = """
                    Filtros:
                    - categoryId: coincidencia exacta con la categoría.
                    - sellerId: coincidencia exacta con el vendedor.
                    - q: texto contenido en el título (case-insensitive).
                    Paginación:
                    - page: índice base 0 (opcional).
                    - elements: tamaño de página (opcional, default=5 si llega page sin elements).
                    Si no se envían page ni elements, se devuelven todos los productos en una única página.
                    """)
    @ApiResponse(responseCode = "200", description = "Página de productos enriquecidos",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/enriched")
    public Mono<ResponseEntity<PageResponse<ItemEnrichedResponse>>> getEnrichedPage(
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "sellerId", required = false) String sellerId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "elements", required = false) Integer elements) {
        return service.enrichedPage(categoryId, sellerId, q, page, elements)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }
}
