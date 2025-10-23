package org.mercadolibre.camilo.category.service;

import org.mercadolibre.camilo.category.dto.BreadcrumbNode;
import org.mercadolibre.camilo.category.dto.CategoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CategoryService {


    /**
     * Obtiene una categoría por id con campos derivados:
     * breadcrumb y childrenCount.
     */
    Mono<CategoryResponse> getWithDerived(String id);

    /**
     * Devuelve el breadcrumb (pathFromRoot) de la categoría dada.
     */
    Mono<List<BreadcrumbNode>> breadcrumb(String id);

    /**
     * Lista categorías. Si {@code parentId} es null/blanco, devuelve todas.
     * Si viene informado, lista solo los hijos del {@code parentId}.
     */
    Flux<CategoryResponse> findAll(String parentId);

}
