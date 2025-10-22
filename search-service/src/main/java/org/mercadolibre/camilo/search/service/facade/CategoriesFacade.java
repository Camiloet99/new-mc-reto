package org.mercadolibre.camilo.search.service.facade;

import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CategoriesFacade {
    Mono<List<CategoryResponse.BreadcrumbNode>> breadcrumb(String categoryId);
}
