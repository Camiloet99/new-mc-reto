package org.mercadolibre.camilo.category.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mercadolibre.camilo.category.dto.BreadcrumbNode;
import org.mercadolibre.camilo.category.dto.CategoryResponse;
import org.mercadolibre.camilo.category.model.ErrorResponse;
import org.mercadolibre.camilo.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "Categories", description = "Operaciones de consulta de categorías")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

    private final CategoryService service;

    @Operation(summary = "Lista categorías (opcionalmente hijos directos por parentId)",
            description = "Obtiene todas las categorías o, si se especifica parentId, únicamente los hijos directos.")
    @ApiResponse(responseCode = "200", description = "Lista de categorías (puede ser vacía)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryResponse.class))))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping
    public ResponseEntity<Flux<CategoryResponse>> getAll(
            @RequestParam(value = "parentId", required = false) String parentId) {

        Flux<CategoryResponse> body = service.findAll(parentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @Operation(summary = "Obtiene una categoría por id (incluye pathFromRoot y childrenCount)")
    @ApiResponse(responseCode = "200", description = "Categoría encontrada",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class)))
    @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CategoryResponse>> get(@PathVariable String id) {
        return service.getWithDerived(id)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }

    @Operation(summary = "Breadcrumb (pathFromRoot) de una categoría",
            description = "Devuelve el breadcrumb incluyendo el nodo raíz y la propia categoría.")
    @ApiResponse(responseCode = "200", description = "Breadcrumb de la categoría",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BreadcrumbNode.class))))
    @ApiResponse(responseCode = "404", description = "No encontrada",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "400", description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}/breadcrumb")
    public Mono<ResponseEntity<List<BreadcrumbNode>>> breadcrumb(
            @Parameter(description = "Identificador de la categoría")
            @PathVariable String id) {
        return service.breadcrumb(id)
                .map(path -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(path));
    }
}