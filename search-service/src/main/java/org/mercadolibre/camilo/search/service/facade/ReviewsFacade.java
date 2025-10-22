package org.mercadolibre.camilo.search.service.facade;

import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewSummaryResponse;
import reactor.core.publisher.Mono;

public interface ReviewsFacade {
    Mono<ReviewSummaryResponse> summary(String productId);
}
