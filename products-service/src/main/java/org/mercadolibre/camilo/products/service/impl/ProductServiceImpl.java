package org.mercadolibre.camilo.products.service.impl;

import org.mercadolibre.camilo.products.dto.PageResponse;
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

import java.util.List;
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

    @Override
    public Mono<PageResponse<ProductResponse>> findAllPaged(
            String categoryId,
            String sellerId,
            String query,
            Integer pageNumber,
            Integer pageSize) {

        final String normalizedCategoryId = normalize(categoryId);
        final String normalizedSellerId = normalize(sellerId);
        final String normalizedQuery = normalize(query);

        log.info("ProductService.findAllPaged | filters | categoryId='{}' sellerId='{}' q='{}' page={} size={}",
                normalizedCategoryId, normalizedSellerId, normalizedQuery, pageNumber, pageSize);

        if (normalizedCategoryId != null && normalizedCategoryId.isBlank()) {
            return Mono.error(new InvalidRequestException("categoryId must not be blank if provided"));
        }
        if (normalizedSellerId != null && normalizedSellerId.isBlank()) {
            return Mono.error(new InvalidRequestException("sellerId must not be blank if provided"));
        }
        if (normalizedQuery != null && normalizedQuery.length() < 2) {
            return Mono.error(new InvalidRequestException("q must have at least 2 characters"));
        }

        Predicate<Product> filterPredicate = product -> true;

        if (normalizedCategoryId != null) {
            filterPredicate = filterPredicate.and(product ->
                    Objects.equals(normalizedCategoryId, product.getCategoryId()));
        }

        if (normalizedSellerId != null) {
            filterPredicate = filterPredicate.and(product ->
                    Objects.equals(normalizedSellerId, product.getSellerId()));
        }

        if (normalizedQuery != null) {
            filterPredicate = filterPredicate.and(product -> {
                String title = product.getTitle();
                return title != null && title.toLowerCase(Locale.ROOT).contains(normalizedQuery);
            });
        }

        final boolean paginationRequested = (pageNumber != null) || (pageSize != null);
        final int currentPage = (pageNumber == null || pageNumber < 0) ? 0 : pageNumber;
        final int elementsPerPage = (pageSize == null || pageSize <= 0) ? 5 : pageSize;

        return Flux.fromIterable(repo.findAll())
                .filter(filterPredicate)
                .map(ProductResponse::from)
                .collectList()
                .map(filteredProducts -> {
                    final long totalItems = filteredProducts.size();

                    if (!paginationRequested) {
                        return PageResponse.<ProductResponse>builder()
                                .page(0)
                                .size((int) totalItems)
                                .totalItems(totalItems)
                                .totalPages(1)
                                .hasPrev(false)
                                .hasNext(false)
                                .items(filteredProducts)
                                .build();
                    }

                    long startIndex = (long) currentPage * elementsPerPage;
                    if (startIndex >= totalItems && totalItems > 0) {
                        int lastPageIndex = (int) ((totalItems - 1) / elementsPerPage);
                        return buildPageResponse(filteredProducts, lastPageIndex, elementsPerPage, totalItems);
                    }

                    return buildPageResponse(filteredProducts, currentPage, elementsPerPage, totalItems);
                })
                .doOnSuccess(page -> log.debug("ProductService.findAllPaged | page={} size={} total={}",
                        page.getPage(), page.getSize(), page.getTotalItems()))
                .doOnError(error -> log.error("ProductService.findAllPaged | error | type={} | msg={}",
                        error.getClass().getSimpleName(), error.getMessage()));
    }

    private PageResponse<ProductResponse> buildPageResponse(
            List<ProductResponse> allProducts,
            int currentPage,
            int elementsPerPage,
            long totalItems) {

        int startIndex = Math.toIntExact(Math.min((long) currentPage * elementsPerPage, totalItems));
        int endIndex = Math.toIntExact(Math.min(startIndex + (long) elementsPerPage, totalItems));
        List<ProductResponse> pageItems = allProducts.subList(startIndex, endIndex);

        int totalPages = totalItems == 0 ? 0 : (int) Math.ceil((double) totalItems / elementsPerPage);
        boolean hasPreviousPage = currentPage > 0 && totalItems > 0;
        boolean hasNextPage = (currentPage + 1) < totalPages;

        return PageResponse.<ProductResponse>builder()
                .page(currentPage)
                .size(elementsPerPage)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasPrev(hasPreviousPage)
                .hasNext(hasNextPage)
                .items(pageItems)
                .build();
    }


    private String normalize(String string) {
        if (string == null) return null;
        String trimmed = string.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }
}