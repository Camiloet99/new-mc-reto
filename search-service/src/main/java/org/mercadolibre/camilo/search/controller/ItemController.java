package org.mercadolibre.camilo.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import org.mercadolibre.camilo.search.model.ErrorResponse;
import org.mercadolibre.camilo.search.service.impl.ItemServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Items", description = "Operaciones relacionadas con los productos")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
public class ItemController {

    private final ItemServiceImpl service;

    @Operation(
            summary = "Obtiene el detalle básico del producto",
            description = "Devuelve información esencial del producto: título, precio, imágenes, condición, stock, atributos y breadcrumb de categoría."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Detalle básico del producto encontrado",
            content = @Content(schema = @Schema(implementation = ItemBasicResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ItemBasicResponse>> getBasic(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable String id) {
        return service.basic(id)
                .map(body -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body));
    }

    @Operation(
            summary = "Obtiene el detalle enriquecido del producto",
            description = "Devuelve información extendida del producto, combinando datos básicos con reseñas, categorías, vendedores u otros metadatos."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Detalle enriquecido del producto encontrado",
            content = @Content(schema = @Schema(implementation = ItemEnrichedResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @GetMapping("/{id}/enriched")
    public Mono<ResponseEntity<ItemEnrichedResponse>> getEnriched(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable String id) {
        return service.enriched(id)
                .map(body -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body));
    }
}
