package org.mercadolibre.camilo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.mercadolibre.camilo.dto.SellerResponse;
import org.mercadolibre.camilo.model.ErrorResponse;
import org.mercadolibre.camilo.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Sellers", description = "Operaciones relacionadas con vendedores")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/sellers", produces = MediaType.APPLICATION_JSON_VALUE)
public class SellerController {

    private final SellerService service;

    @Operation(
            summary = "Lista todos los vendedores disponibles",
            description = "Devuelve un flujo (puede ser vacío) con los vendedores registrados."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de vendedores (puede ser vacío)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SellerResponse.class)))
    )
    @ApiResponse(
            responseCode = "500",
            description = "Error inesperado",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @GetMapping
    public ResponseEntity<Flux<SellerResponse>> getAll() {
        Flux<SellerResponse> body = service.findAll();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @Operation(
            summary = "Obtiene un vendedor por ID",
            description = "Devuelve la información del vendedor si existe."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Vendedor encontrado",
            content = @Content(schema = @Schema(implementation = SellerResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Vendedor no encontrado",
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
    public Mono<ResponseEntity<SellerResponse>> get(
            @Parameter(description = "Identificador del vendedor", required = true)
            @PathVariable String id) {
        return service.get(id)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }
}
