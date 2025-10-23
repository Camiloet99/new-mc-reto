package com.mercadolibre.camilo.review.service;

import com.mercadolibre.camilo.review.dto.ReviewResponse;
import com.mercadolibre.camilo.review.dto.ReviewSummaryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Casos de uso de reviews.
 */
public interface ReviewService {

    /**
     * Lista las reseñas de un producto.
     *
     * @param productId id del producto (no nulo/blank)
     */
    Flux<ReviewResponse> findByProduct(String productId);

    /**
     * Devuelve el resumen estadístico de reseñas para un producto.
     *
     * @param productId id del producto (no nulo/blank)
     */
    Mono<ReviewSummaryResponse> summary(String productId);
}
