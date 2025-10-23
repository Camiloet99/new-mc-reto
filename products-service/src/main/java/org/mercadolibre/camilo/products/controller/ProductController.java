package org.mercadolibre.camilo.products.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mercadolibre.camilo.products.dto.PageResponse;
import org.mercadolibre.camilo.products.dto.ProductResponse;
import org.mercadolibre.camilo.products.model.ErrorResponse;
import org.mercadolibre.camilo.products.service.impl.ProductServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "Products", description = "Operaciones relacionadas con los productos")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductServiceImpl service;

    @Operation(summary = "Lista productos con filtros y paginación opcional", description = """
            Filtros:
            - categoryId: coincidencia exacta con la categoría.
            - sellerId: coincidencia exacta con el vendedor.
            - q: texto contenido en el título (case-insensitive).
            Paginación:
            - page: índice base 0 (opcional).
            - elements: tamaño de página (opcional, default=5 si llega page sin elements).
            Si no se envían page ni elements, se devuelven todos los productos en una única página.
            """)
    @ApiResponse(responseCode = "200", description = "Página de productos")
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public Mono<ResponseEntity<PageResponse<ProductResponse>>> getAll(
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "sellerId", required = false) String sellerId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "elements", required = false) Integer elements) {

        return service.findAllPaged(categoryId, sellerId, q, page, elements)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }

    @Operation(summary = "Obtiene un producto por su identificador",
            description = "Devuelve los datos completos de un producto existente.")
    @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiResponse(responseCode = "404", description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductResponse>> get(
            @Parameter(description = "Identificador único del producto", required = true)
            @PathVariable String id) {
        return service.get(id)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }

    @Operation(summary = "Búsqueda fuzzy de productos por título", description = """
            Realiza fuzzy search sobre el título de los productos.
            - query: texto a buscar (obligatorio, min 2 chars)
            - limit: máximo de resultados (opcional, default 20, tope 100)
            El resultado viene ordenado por relevancia (score desc).
            """)
    @ApiResponse(responseCode = "200", description = "Listado de productos por relevancia",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/search")
    public ResponseEntity<Flux<ProductResponse>> search(
            @Parameter(description = "Texto de búsqueda (min 2 caracteres)", required = true)
            @RequestParam("query") String query,
            @Parameter(description = "Máximo de resultados (default 20, tope 100)")
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        Flux<ProductResponse> body = service.searchFuzzy(query, limit);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @Operation(summary = "Autocomplete fuzzy de títulos de productos", description = """
            Devuelve una lista de títulos que coinciden por fuzzy search.
            - query: texto a buscar (obligatorio, min 2 chars)
            - limit: máximo de sugerencias (opcional, default 10, tope 50)
            Resultados únicos y ordenados por relevancia (score desc).
            """)
    @ApiResponse(responseCode = "200", description = "Listado de títulos",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/autocomplete")
    public Mono<ResponseEntity<List<String>>> autocomplete(
            @Parameter(description = "Texto de búsqueda (min 2 caracteres)", required = true)
            @RequestParam("query") String query,
            @Parameter(description = "Máximo de resultados (default 10, tope 50)")
            @RequestParam(value = "limit", required = false) Integer limit
    ) {
        return service.autocompleteTitles(query, limit)
                .collectList()
                .map(list -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(list));
    }
}