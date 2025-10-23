package org.mercadolibre.camilo.search.service;

import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.PageResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import reactor.core.publisher.Mono;

public interface ItemService {

    /**
     * Obtiene el detalle básico de un producto.
     * <p>
     * Incluye la información base del producto junto con el breadcrumb de su categoría,
     * lo cual permite reconstruir la jerarquía completa dentro del árbol de categorías.
     *
     * @param productId identificador único del producto a consultar.
     * @return un {@link Mono} que emite un {@link ItemBasicResponse} con la información básica del producto
     * y el breadcrumb de categoría, o un error si el producto no existe o algún servicio falla.
     */
    Mono<ItemBasicResponse> basic(String productId);

    /**
     * Obtiene el detalle enriquecido de un producto individual.
     * <p>
     * Combina la información básica del producto con datos de vendedor, reseñas y preguntas frecuentes
     * (Q&A), generando una vista completa del producto para visualización en detalle.
     *
     * @param productId identificador único del producto a consultar.
     * @return un {@link Mono} que emite un {@link ItemEnrichedResponse} conteniendo todos los datos
     * combinados, o un error en caso de fallo de comunicación o ausencia de datos críticos.
     */
    Mono<ItemEnrichedResponse> enriched(String productId);

    /**
     * Obtiene un listado paginado de productos enriquecidos, aplicando filtros opcionales.
     * <p>
     * Este método permite recuperar múltiples productos con toda su información compuesta
     * (detalles, vendedor, reseñas y Q&A) de forma paginada y reactiva. Los filtros son opcionales
     * y pueden incluir coincidencias por categoría, vendedor o texto en el título.
     *
     * @param categoryId identificador de la categoría (opcional).
     * @param sellerId   identificador del vendedor (opcional).
     * @param q          texto de búsqueda parcial en el título del producto (opcional, case-insensitive).
     * @param page       índice de página (base 0, opcional).
     * @param elements   tamaño de página (cantidad de elementos por página, opcional).
     * @return un {@link Mono} que emite un {@link PageResponse} con una lista de {@link ItemEnrichedResponse}
     * y los metadatos de paginación (número de página, total de elementos, etc.).
     */
    Mono<PageResponse<ItemEnrichedResponse>> enrichedPage(
            String categoryId,
            String sellerId,
            String q,
            Integer page,
            Integer elements
    );
}