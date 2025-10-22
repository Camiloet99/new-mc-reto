package org.mercadolibre.camilo.search.service;

import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import reactor.core.publisher.Mono;

/**
 * Orquesta la obtención de detalles de producto
 * en formato básico y enriquecido.
 */
public interface ItemService {

    /**
     * Detalle básico del producto (producto + breadcrumb de categoría).
     *
     * @param productId id del producto
     * @return respuesta básica reactiva
     */
    Mono<ItemBasicResponse> basic(String productId);

    /**
     * Detalle enriquecido (producto + breadcrumb + seller + reviews + Q&A).
     *
     * @param productId id del producto
     * @return respuesta enriquecida reactiva
     */
    Mono<ItemEnrichedResponse> enriched(String productId);
}
