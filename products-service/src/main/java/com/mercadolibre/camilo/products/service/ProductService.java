package com.mercadolibre.camilo.products.service;

import com.mercadolibre.camilo.products.dto.ProductResponse;
import com.mercadolibre.camilo.products.exception.NotFoundException;
import com.mercadolibre.camilo.products.model.Product;
import com.mercadolibre.camilo.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    public Mono<ProductResponse> get(String id) {
        return Mono.justOrEmpty(repo.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("Product '%s' not found".formatted(id))))
                .map(ProductResponse::from);
    }

    public Flux<ProductResponse> findAll(String categoryId, String sellerId, String q) {
        String needle = q == null ? null : q.toLowerCase(Locale.ROOT);
        Predicate<Product> pred = p -> true;

        if (categoryId != null && !categoryId.isBlank()) {
            pred = pred.and(p -> categoryId.equals(p.getCategoryId()));
        }
        if (sellerId != null && !sellerId.isBlank()) {
            pred = pred.and(p -> sellerId.equals(p.getSellerId()));
        }
        if (needle != null && !needle.isBlank()) {
            pred = pred.and(p -> p.getTitle() != null && p.getTitle().toLowerCase(Locale.ROOT).contains(needle));
        }

        return Flux.fromIterable(repo.findAll())
                .filter(pred)
                .map(ProductResponse::from);
    }
}