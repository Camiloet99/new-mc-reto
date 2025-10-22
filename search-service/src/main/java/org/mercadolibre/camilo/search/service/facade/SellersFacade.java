package org.mercadolibre.camilo.search.service.facade;

import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import reactor.core.publisher.Mono;

public interface SellersFacade {
    Mono<SellerResponse> getById(String sellerId);
}
