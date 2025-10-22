package org.mercadolibre.camilo.search.service.facade;

import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface QaFacade {
    Mono<List<QaResponse>> listByProduct(String productId);
}
