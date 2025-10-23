package org.mercadolibre.camilo.search.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.PageResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import org.mercadolibre.camilo.search.service.ItemService;
import org.mercadolibre.camilo.search.service.facade.*;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewResponse;
import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final int ENRICH_CONCURRENCY = 8;

    private final ProductsFacade products;
    private final CategoriesFacade categories;
    private final SellersFacade sellers;
    private final ReviewsFacade reviews;
    private final QaFacade qa;

    @Override
    public Mono<ItemBasicResponse> basic(String productId) {
        return products.getById(productId)
                .flatMap(prod -> categories.breadcrumb(prod.getCategoryId())
                        .defaultIfEmpty(Collections.emptyList())
                        .map(bc -> ItemBasicResponse.from(prod, bc)));
    }

    @Override
    public Mono<ItemEnrichedResponse> enriched(String productId) {
        return products.getById(productId)
                .flatMap(this::enrich);
    }

    @Override
    public Mono<PageResponse<ItemEnrichedResponse>> enrichedPage(
            String categoryId,
            String sellerId,
            String q,
            Integer page,
            Integer elements
    ) {
        return products.getAll(categoryId, sellerId, q, page, elements)
                .flatMap(srcPage -> Flux.fromIterable(srcPage.getItems())
                        .flatMap(this::enrich, ENRICH_CONCURRENCY)
                        .collectList()
                        .map(enrichedItems -> PageResponse.<ItemEnrichedResponse>builder()
                                .page(srcPage.getPage())
                                .size(srcPage.getSize())
                                .totalItems(srcPage.getTotalItems())
                                .totalPages(srcPage.getTotalPages())
                                .hasPrev(srcPage.isHasPrev())
                                .hasNext(srcPage.isHasNext())
                                .items(enrichedItems)
                                .build()
                        )
                )
                .doOnError(ex -> log.error("ItemService.enrichedPage | error enriching page | msg={}", ex.getMessage(), ex))
                .doOnSuccess(res -> log.info("ItemService.enrichedPage | enriched {} items | page={}",
                        res.getItems() != null ? res.getItems().size() : 0, res.getPage()));
    }

    private Mono<ItemEnrichedResponse> enrich(ProductResponse prod) {
        return Mono.zip(
                        categories.breadcrumb(prod.getCategoryId())
                                .onErrorReturn(Collections.emptyList()),
                        sellers.getById(prod.getSellerId())
                                .onErrorReturn(SellerResponse.builder().build()),
                        reviews.list(prod.getId())
                                .onErrorReturn(Collections.emptyList()),
                        qa.listByProduct(prod.getId())
                                .onErrorReturn(Collections.emptyList())
                )
                .map(tuple -> {
                    List<CategoryResponse.BreadcrumbNode> breadcrumb = tuple.getT1();
                    SellerResponse seller = tuple.getT2();
                    List<ReviewResponse> reviewList = tuple.getT3();
                    List<QaResponse> qaList = tuple.getT4();

                    ItemBasicResponse basic = ItemBasicResponse.from(prod, breadcrumb);
                    return ItemEnrichedResponse.builder()
                            .basic(basic)
                            .seller(seller)
                            .reviews(reviewList)
                            .qa(qaList)
                            .build();
                })
                .doOnError(ex -> log.error("ItemService.enrich | id={} | type={} | msg={}",
                        prod.getId(), ex.getClass().getSimpleName(), ex.getMessage()))
                .onErrorResume(ex -> categories.breadcrumb(prod.getCategoryId())
                        .onErrorReturn(Collections.emptyList())
                        .map(breadcrumbNodes -> ItemEnrichedResponse.builder()
                                .basic(ItemBasicResponse.from(prod, breadcrumbNodes))
                                .seller(SellerResponse.builder().build())
                                .reviews(Collections.emptyList())
                                .qa(Collections.emptyList())
                                .build()));
    }
}