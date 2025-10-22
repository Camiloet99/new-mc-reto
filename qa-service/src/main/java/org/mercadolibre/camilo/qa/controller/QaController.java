package org.mercadolibre.camilo.qa.controller;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.qa.dto.QuestionResponse;
import org.mercadolibre.camilo.qa.service.QaService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/qa", produces = MediaType.APPLICATION_JSON_VALUE)
public class QaController {

    private final QaService service;

    /**
     * Lista preguntas y sus respuestas para un producto.
     *
     * @param productId identificador del producto
     * @return {@link ResponseEntity} con un {@link Flux} de {@link QuestionResponse}
     */
    @GetMapping
    public ResponseEntity<Flux<QuestionResponse>> list(@RequestParam String productId) {
        Flux<QuestionResponse> body = service.listByProduct(productId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    /**
     * Obtiene una pregunta específica (incluye respuestas).
     *
     * @param questionId identificador de la pregunta
     * @return {@link Mono} de {@link ResponseEntity} con {@link QuestionResponse}
     */
    @GetMapping("/{questionId}")
    public Mono<ResponseEntity<QuestionResponse>> get(@PathVariable String questionId) {
        return service.get(questionId)
                .map(resp -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp));
    }
}