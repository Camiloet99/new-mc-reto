package org.mercadolibre.camilo.products.service.impl;

import org.mercadolibre.camilo.products.dto.ProductResponse;
import org.mercadolibre.camilo.products.exception.InvalidRequestException;
import org.mercadolibre.camilo.products.exception.ProductNotFoundException;
import org.mercadolibre.camilo.products.model.Product;
import org.mercadolibre.camilo.products.repository.impl.ProductRepositoryImpl;
import org.mercadolibre.camilo.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepositoryImpl repo;

    @Override
    public Mono<ProductResponse> get(String id) {
        if (id == null || id.isBlank()) {
            log.warn("ProductService.get | invalid id (blank)");
            return Mono.error(new InvalidRequestException("Product id must not be blank"));
        }

        log.info("ProductService.get | fetching product | id={}", id);
        return Mono.defer(() -> Mono.justOrEmpty(repo.findById(id)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("ProductService.get | product not found | id={}", id);
                    return Mono.error(new ProductNotFoundException(id));
                }))
                .map(p -> {
                    log.debug("ProductService.get | product loaded | id={} title={}", p.getId(), p.getTitle());
                    return ProductResponse.from(p);
                })
                .doOnError(ex ->
                        log.error("ProductService.get | error | id={} | type={} | msg={}",
                                id, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @Override
    public Flux<ProductResponse> findAll(String categoryId, String sellerId, String q) {
        final String cat = normalize(categoryId);
        final String sel = normalize(sellerId);
        final String needle = normalize(q);
        log.info("ProductService.findAll | filters | categoryId='{}' sellerId='{}' q='{}'", cat, sel, needle);

        if (cat != null && cat.isBlank()) {
            return Flux.error(new InvalidRequestException("categoryId must not be blank if provided"));
        }
        if (sel != null && sel.isBlank()) {
            return Flux.error(new InvalidRequestException("sellerId must not be blank if provided"));
        }
        if (needle != null && needle.length() < 2) {
            return Flux.error(new InvalidRequestException("q must have at least 2 characters"));
        }

        Predicate<Product> pred = p -> true;

        if (cat != null) {
            pred = pred.and(p -> Objects.equals(cat, p.getCategoryId()));
        }
        if (sel != null) {
            pred = pred.and(p -> Objects.equals(sel, p.getSellerId()));
        }
        if (needle != null) {
            pred = pred.and(p -> {
                String title = p.getTitle();
                return title != null && title.toLowerCase(Locale.ROOT).contains(needle);
            });
        }

        return Flux.fromIterable(repo.findAll())
                .filter(pred)
                .map(ProductResponse::from)
                .doOnSubscribe(s -> log.debug("ProductService.findAll | querying repository"))
                .doOnNext(r -> log.trace("ProductService.findAll | hit | id={} title={}", r.getId(), r.getTitle()))
                .doOnComplete(() -> log.debug("ProductService.findAll | completed"))
                .doOnError(ex -> log.error("ProductService.findAll | error | type={} | msg={}",
                        ex.getClass().getSimpleName(), ex.getMessage()));
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? "" : t;
    }
}