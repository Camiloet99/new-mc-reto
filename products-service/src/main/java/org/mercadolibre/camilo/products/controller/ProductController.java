package org.mercadolibre.camilo.products.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mercadolibre.camilo.products.dto.ProductResponse;
import org.mercadolibre.camilo.products.model.ErrorResponse;
import org.mercadolibre.camilo.products.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Products", description = "Operaciones relacionadas con los productos")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductServiceImpl service;

    @Operation(
            summary = "Lista productos aplicando filtros opcionales",
            description = """
                    Permite filtrar productos según:
                    - categoryId: coincidencia exacta con la categoría.
                    - sellerId: coincidencia exacta con el vendedor.
                    - q: texto contenido en el título (case-insensitive).
                    Si no se envían filtros, se listan todos los productos disponibles.
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de productos (puede venir vacío)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)))
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
    @GetMapping
    public ResponseEntity<Flux<ProductResponse>> getAll(
            @Parameter(description = "Identificador de la categoría (opcional)")
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @Parameter(description = "Identificador del vendedor (opcional)")
            @RequestParam(value = "sellerId", required = false) String sellerId,
            @Parameter(description = "Texto de búsqueda en el título (opcional)")
            @RequestParam(value = "q", required = false) String q) {

        Flux<ProductResponse> body = service.findAll(categoryId, sellerId, q);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @Operation(
            summary = "Obtiene un producto por su identificador",
            description = "Devuelve los datos completos de un producto existente."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))
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
    public Mono<ResponseEntity<ProductResponse>> get(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable String id) {
        return service.get(id)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }
}