package org.mercadolibre.camilo.qa.service;

import org.mercadolibre.camilo.qa.dto.QuestionResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QaService {

    /**
     * Lista preguntas de un producto.
     *
     * @param productId id del producto (no nulo/blank)
     */
    Flux<QuestionResponse> listByProduct(String productId);

    /**
     * Obtiene una pregunta por id.
     *
     * @param questionId id de la pregunta (no nulo/blank)
     */
    Mono<QuestionResponse> get(String questionId);
}
