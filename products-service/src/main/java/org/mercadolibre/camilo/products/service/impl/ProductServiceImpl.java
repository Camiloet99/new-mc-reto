package org.mercadolibre.camilo.products.service.impl;

import org.mercadolibre.camilo.products.dto.ProductResponse;
import org.mercadolibre.camilo.products.exception.InvalidRequestException;
import org.mercadolibre.camilo.products.exception.ProductNotFoundException;
import org.mercadolibre.camilo.products.model.Product;
import org.mercadolibre.camilo.products.model.Scored;
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

import static org.mercadolibre.camilo.products.util.FuzzyUtils.score;

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

    @Override
    public Flux<ProductResponse> searchFuzzy(String query, Integer limit) {
        final String normalizedQuery = normalize(query);
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            log.warn("ProductService.searchFuzzy | invalid query (blank)");
            return Flux.error(new InvalidRequestException("query must not be blank"));
        }
        if (normalizedQuery.length() < 2) {
            return Flux.error(new InvalidRequestException("query must have at least 2 characters"));
        }
        final int max = limit == null ? 20 : Math.max(1, Math.min(limit, 100));
        final double threshold = 0.35;

        log.info("ProductService.searchFuzzy | normalizedQuery='{}' limit={}", normalizedQuery, max);

        return Flux.fromIterable(repo.findAll())
                .map(p -> {
                    double score = score(normalizedQuery, p.getTitle());
                    return new Scored<>(p, score);
                })
                .filter(s -> s.score() >= threshold)
                .sort((a, b) -> Double.compare(b.score(), a.score()))
                .take(max)
                .map(s -> {
                    log.trace("ProductService.searchFuzzy | hit id={} title='{}' score={}",
                            s.value().getId(), s.value().getTitle(), String.format("%.3f", s.score()));
                    return ProductResponse.from(s.value());
                })
                .doOnComplete(() -> log.debug("ProductService.searchFuzzy | completed"))
                .doOnError(ex -> log.error("ProductService.searchFuzzy | error | type={} | msg={}",
                        ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @Override
    public Flux<String> autocompleteTitles(String query, Integer limit) {
        final String q = normalize(query);
        if (q == null || q.isBlank()) {
            log.warn("ProductService.autocompleteTitles | invalid query (blank)");
            return Flux.error(new InvalidRequestException("query must not be blank"));
        }
        if (q.length() < 2) {
            return Flux.error(new InvalidRequestException("query must have at least 2 characters"));
        }

        final int max = limit == null ? 10 : Math.max(1, Math.min(limit, 50));
        final double threshold = 0.30;

        log.info("ProductService.autocompleteTitles | q='{}' limit={}", q, max);

        return Flux.fromIterable(repo.findAll())
                .map(p -> {
                    String title = p.getTitle();
                    double base = score(q, title);
                    String normTitle = title == null ? "" : title.toLowerCase(Locale.ROOT).trim();
                    double boost = (normTitle.startsWith(q)) ? 0.15 : 0.0;
                    return new Scored<>(title, Math.min(1.0, base + boost));
                })
                .filter(s -> s.value() != null && s.score() >= threshold)
                .sort((a, b) -> {
                    int cmp = Double.compare(b.score(), a.score());
                    return (cmp != 0) ? cmp : a.value().compareToIgnoreCase(b.value());
                })
                .map(Scored::value)
                .distinct(t -> t.toLowerCase(Locale.ROOT))
                .take(max)
                .doOnNext(t -> log.trace("ProductService.autocompleteTitles | hit title='{}'", t))
                .doOnComplete(() -> log.debug("ProductService.autocompleteTitles | completed"))
                .doOnError(ex -> log.error("ProductService.autocompleteTitles | error | type={} | msg={}",
                        ex.getClass().getSimpleName(), ex.getMessage()));
    }

    private String normalize(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? "" : t;
    }
}