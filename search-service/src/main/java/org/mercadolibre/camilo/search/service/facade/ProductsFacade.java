package org.mercadolibre.camilo.search.service.facade;

import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;
import reactor.core.publisher.Mono;

public interface ProductsFacade {
    Mono<ProductResponse> getById(String productId);
}
