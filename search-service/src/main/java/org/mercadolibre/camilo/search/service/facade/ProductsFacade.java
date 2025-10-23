package org.mercadolibre.camilo.search.service.facade;

import org.mercadolibre.camilo.search.dto.PageResponse;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;
import reactor.core.publisher.Mono;

public interface ProductsFacade {
    Mono<ProductResponse> getById(String productId);

    Mono<PageResponse<ProductResponse>> getAll(String categoryId, String sellerId,
                                               String q, Integer page, Integer elements
    );
}
