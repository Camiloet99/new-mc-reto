package com.mercadolibre.camilo.category.service.impl;

import com.mercadolibre.camilo.category.dto.BreadcrumbNode;
import com.mercadolibre.camilo.category.dto.CategoryResponse;
import com.mercadolibre.camilo.category.exception.NotFoundException;
import com.mercadolibre.camilo.category.model.Category;
import com.mercadolibre.camilo.category.repository.impl.CategoryRepositoryImpl;
import com.mercadolibre.camilo.category.utils.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl {

    private final CategoryRepositoryImpl repo;

    public Mono<CategoryResponse> getWithDerived(String id) {
        Preconditions.requireNonBlank(id, "id must not be null or blank");
        return Mono.justOrEmpty(repo.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("Category '%s' not found".formatted(id))))
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .parentId(category.getParentId())
                        .pathFromRoot(buildPath(category))
                        .childrenCount(repo.childrenOf(category.getId()).size())
                        .build());
    }

    public Mono<List<BreadcrumbNode>> breadcrumb(String id) {
        Preconditions.requireNonBlank(id, "id must not be null or blank");
        return Mono.justOrEmpty(repo.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("Category '%s' not found".formatted(id))))
                .map(this::buildPath);
    }

    public Flux<CategoryResponse> findAll(String parentId) {
        if (parentId == null || parentId.isBlank()) {
            return Flux.fromIterable(repo.getById().values())
                    .map(cat -> CategoryResponse.builder()
                            .id(cat.getId())
                            .name(cat.getName())
                            .parentId(cat.getParentId())
                            .pathFromRoot(buildPath(cat))
                            .childrenCount(repo.childrenOf(cat.getId()).size())
                            .build());
        }
        return Flux.fromIterable(repo.childrenOf(parentId))
                .map(cat -> CategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .parentId(cat.getParentId())
                        .pathFromRoot(buildPath(cat))
                        .childrenCount(repo.childrenOf(cat.getId()).size())
                        .build());
    }

    private List<BreadcrumbNode> buildPath(Category leaf) {
        List<BreadcrumbNode> path = new ArrayList<>();
        Category cur = leaf;
        while (cur != null) {
            path.add(0, BreadcrumbNode.builder().id(cur.getId()).name(cur.getName()).build());
            cur = cur.getParentId() == null ? null : repo.findById(cur.getParentId()).orElse(null);
        }
        return path;
    }
}
