package org.mercadolibre.camilo.qa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.qa.dto.QuestionResponse;
import org.mercadolibre.camilo.qa.model.ErrorResponse;
import org.mercadolibre.camilo.qa.service.QaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Q&A", description = "Operaciones de preguntas y respuestas de los productos")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/qa", produces = MediaType.APPLICATION_JSON_VALUE)
public class QaController {

    private final QaService service;

    @Operation(
            summary = "Lista las preguntas y respuestas de un producto",
            description = "Devuelve un flujo con las preguntas y sus respuestas asociadas a un producto específico."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Listado de preguntas (puede venir vacío)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = QuestionResponse.class)))
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
    public ResponseEntity<Flux<QuestionResponse>> list(
            @Parameter(description = "Identificador del producto", required = true)
            @RequestParam String productId) {
        Flux<QuestionResponse> body = service.listByProduct(productId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @Operation(
            summary = "Obtiene una pregunta específica",
            description = "Devuelve una pregunta junto con sus respuestas, si existen."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pregunta encontrada",
            content = @Content(schema = @Schema(implementation = QuestionResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Pregunta no encontrada",
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
    @GetMapping("/{questionId}")
    public Mono<ResponseEntity<QuestionResponse>> get(
            @Parameter(description = "Identificador de la pregunta", required = true)
            @PathVariable String questionId) {
        return service.get(questionId)
                .map(resp -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp));
    }
}