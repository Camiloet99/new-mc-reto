package com.mercadolibre.camilo.review.controller;

import com.mercadolibre.camilo.review.dto.ReviewResponse;
import com.mercadolibre.camilo.review.dto.ReviewSummaryResponse;
import com.mercadolibre.camilo.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/reviews", produces = MediaType.APPLICATION_JSON_VALUE)
public class ReviewController {

    private final ReviewService service;

    /**
     * Lista las reseñas de un producto.
     */
    @GetMapping
    public ResponseEntity<Flux<ReviewResponse>> list(@RequestParam String productId) {
        Flux<ReviewResponse> body = service.findByProduct(productId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    /**
     * Devuelve el resumen de reseñas (promedio, cantidad e histograma 1..5).
     */
    @GetMapping("/summary")
    public Mono<ResponseEntity<ReviewSummaryResponse>> summary(@RequestParam String productId) {
        return service.summary(productId)
                .map(resp -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(resp));
    }
}
