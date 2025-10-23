package org.mercadolibre.camilo.products.service;

import org.mercadolibre.camilo.products.dto.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de negocio para operaciones sobre productos.
 */
public interface ProductService {

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id identificador del producto (no nulo/blank)
     * @return producto envuelto en {@link Mono}
     */
    Mono<ProductResponse> get(String id);

    /**
     * Busca productos con filtros opcionales.
     *
     * @param categoryId id de categoría (opcional)
     * @param sellerId   id de vendedor (opcional)
     * @param q          texto de búsqueda aplicado al título (opcional)
     * @return flujo reactivo con los resultados
     */
    Flux<ProductResponse> findAll(String categoryId, String sellerId, String q);

    /**
     * Fuzzy search por título. Ordenado desc por score.
     *
     * @param query texto de búsqueda (obligatorio, min 2)
     * @param limit máximo de resultados (opcional, por defecto 20, tope 100)
     */
    Flux<ProductResponse> searchFuzzy(String query, Integer limit);

    /**
     * Autocomplete fuzzy de títulos. Ordenado por relevancia desc, títulos únicos.
     * @param query texto a buscar (obligatorio, min 2)
     * @param limit máximo de títulos (opcional, default 10, tope 50)
     */
    Flux<String> autocompleteTitles(String query, Integer limit);

}
