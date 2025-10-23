package org.mercadolibre.camilo.search.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import org.mercadolibre.camilo.search.service.ItemService;
import org.mercadolibre.camilo.search.service.facade.*;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewResponse;
import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ProductsFacade products;
    private final CategoriesFacade categories;
    private final SellersFacade sellers;
    private final ReviewsFacade reviews;
    private final QaFacade qa;

    public Mono<ItemBasicResponse> basic(String productId) {
        return products.getById(productId).flatMap(prod ->
                        categories.breadcrumb(prod.getCategoryId())
                                .map(bc -> ItemBasicResponse.from(prod, bc)))
                .doOnError(ex ->
                        log.error("ItemService.basic | error fetching basic item info | id=" + productId));
    }

    public Mono<ItemEnrichedResponse> enriched(String productId) {

        return products.getById(productId)
                .flatMap(prod -> Mono.zip(
                                categories.breadcrumb(prod.getCategoryId())
                                        .onErrorReturn(Collections.emptyList()),
                                sellers.getById(prod.getSellerId())
                                        .onErrorReturn(SellerResponse.builder().build()),
                                reviews.list(productId)
                                        .onErrorReturn(Collections.emptyList()),
                                qa.listByProduct(productId)
                                        .onErrorReturn(Collections.emptyList()))
                        .map(data -> {
                            List<CategoryResponse.BreadcrumbNode> breadcrumb = data.getT1();
                            SellerResponse seller = data.getT2();
                            List<ReviewResponse> reviewSummary = data.getT3();
                            List<QaResponse> qaList = data.getT4();

                            ItemBasicResponse basic = ItemBasicResponse.from(prod, breadcrumb);
                            return ItemEnrichedResponse.builder()
                                    .basic(basic)
                                    .seller(seller)
                                    .reviews(reviewSummary)
                                    .qa(qaList)
                                    .build();
                        })
                )
                .doOnError(ex -> log.error("ItemService.enriched | error enriching item | id="
                        + productId + "\n" +
                        "| type=" + ex.getClass().getSimpleName() + "\n" +
                        "| msg=" + ex.getMessage()))
                .doOnSuccess(res -> log.info("ItemService.enriched | successfully enriched item | id={}", productId));
    }
}