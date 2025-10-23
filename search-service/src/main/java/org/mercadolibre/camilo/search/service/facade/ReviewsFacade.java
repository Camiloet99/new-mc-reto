package org.mercadolibre.camilo.search.service.facade;

import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReviewsFacade {
    Mono<List<ReviewResponse>> list(String productId);
}
