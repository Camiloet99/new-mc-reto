package com.mercadolibre.camilo.category.service.impl;

import com.mercadolibre.camilo.category.dto.BreadcrumbNode;
import com.mercadolibre.camilo.category.dto.CategoryResponse;
import com.mercadolibre.camilo.category.exception.CategoryNotFoundException;
import com.mercadolibre.camilo.category.exception.InvalidRequestException;
import com.mercadolibre.camilo.category.model.Category;
import com.mercadolibre.camilo.category.repository.CategoryRepository;
import com.mercadolibre.camilo.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repo;

    @Override
    public Mono<CategoryResponse> getWithDerived(String id) {
        if (id == null || id.isBlank()) {
            log.warn("CategoryService.getWithDerived | invalid id (blank)");
            return Mono.error(new InvalidRequestException("Category id must not be blank"));
        }

        log.info("CategoryService.getWithDerived | fetching category | id={}", id);
        return Mono.defer(() -> Mono.justOrEmpty(repo.findById(id)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("CategoryService.getWithDerived | not found | id={}", id);
                    return Mono.error(new CategoryNotFoundException(id));
                }))
                .map(category -> {
                    List<BreadcrumbNode> path = buildPath(category);
                    int children = repo.childrenOf(category.getId()).size();
                    log.debug("CategoryService.getWithDerived | ok | id={} children={} pathLen={}",
                            category.getId(), children, path.size());
                    return CategoryResponse.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .parentId(category.getParentId())
                            .pathFromRoot(path)
                            .childrenCount(children)
                            .build();
                })
                .doOnError(ex -> log.error("CategoryService.getWithDerived | error | id={} | type={} | msg={}",
                        id, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @Override
    public Mono<List<BreadcrumbNode>> breadcrumb(String id) {
        if (id == null || id.isBlank()) {
            log.warn("CategoryService.breadcrumb | invalid id (blank)");
            return Mono.error(new InvalidRequestException("Category id must not be blank"));
        }

        log.info("CategoryService.breadcrumb | computing | id={}", id);
        return Mono.defer(() -> Mono.justOrEmpty(repo.findById(id)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("CategoryService.breadcrumb | not found | id={}", id);
                    return Mono.error(new CategoryNotFoundException(id));
                }))
                .map(cat -> {
                    List<BreadcrumbNode> path = buildPath(cat);
                    log.debug("CategoryService.breadcrumb | ok | id={} | pathLen={}", id, path.size());
                    return path;
                })
                .doOnError(ex -> log.error("CategoryService.breadcrumb | error | id={} | type={} | msg={}",
                        id, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @Override
    public Flux<CategoryResponse> findAll(String parentId) {
        final String pid = parentId == null ? null : parentId.trim();
        log.info("CategoryService.findAll | parentId='{}'", pid);

        if (pid == null || pid.isEmpty()) {
            return Flux.fromIterable(repo.getById().values())
                    .map(cat -> toResponse(cat))
                    .doOnComplete(() -> log.debug("CategoryService.findAll | all-completed"));
        }

        return Mono.justOrEmpty(repo.findById(pid))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("CategoryService.findAll | parent not found | parentId={}", pid);
                    return Mono.error(new CategoryNotFoundException(pid));
                }))
                .flatMapMany(parent -> Flux.fromIterable(repo.childrenOf(parent.getId()))
                        .map(this::toResponse)
                        .doOnComplete(() -> log.debug("CategoryService.findAll | children-completed | parentId={}", pid))
                )
                .doOnError(ex -> log.error("CategoryService.findAll | error | parentId={} | type={} | msg={}",
                        pid, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    private CategoryResponse toResponse(Category cat) {
        List<BreadcrumbNode> path = buildPath(cat);
        int children = repo.childrenOf(cat.getId()).size();
        return CategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .parentId(cat.getParentId())
                .pathFromRoot(path)
                .childrenCount(children)
                .build();
    }

    /**
     * Construye el breadcrumb desde la hoja hasta la raíz.
     * Si en algún punto falta el padre, detiene el recorrido sin lanzar excepción.
     */
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