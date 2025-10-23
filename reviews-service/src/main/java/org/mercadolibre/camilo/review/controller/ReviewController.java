package org.mercadolibre.camilo.review.controller;

import org.mercadolibre.camilo.review.dto.ReviewResponse;
import org.mercadolibre.camilo.review.dto.ReviewSummaryResponse;
import org.mercadolibre.camilo.review.model.ErrorResponse;
import org.mercadolibre.camilo.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Reviews", description = "Operaciones relacionadas con reseñas de productos")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReviewController {

    private final ReviewService service;

    @Operation(
            summary = "Lista las reseñas de un producto",
            description = "Devuelve un flujo (puede ser vacío) de reseñas asociadas al productId proporcionado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de reseñas (puede ser vacío)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class)))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @GetMapping
    public ResponseEntity<Flux<ReviewResponse>> list(
            @Parameter(description = "Identificador del producto", required = true)
            @RequestParam String productId) {
        Flux<ReviewResponse> body = service.findByProduct(productId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @Operation(
            summary = "Devuelve el resumen de reseñas de un producto",
            description = "Incluye promedio simple, cantidad total e histograma 1..5 (rating -> cantidad)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Resumen de reseñas generado correctamente",
            content = @Content(schema = @Schema(implementation = ReviewSummaryResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Petición inválida",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @GetMapping("/summary")
    public Mono<ResponseEntity<ReviewSummaryResponse>> summary(
            @Parameter(description = "Identificador del producto", required = true)
            @RequestParam String productId) {
        return service.summary(productId)
                .map(resp -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp));
    }
}
