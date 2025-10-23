package org.mercadolibre.camilo.search.service.impl;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

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
                        .map(bc -> ItemBasicResponse.from(prod, bc))
        );
    }

    public Mono<ItemEnrichedResponse> enriched(String productId) {
        return products.getById(productId).flatMap(prod ->
                Mono.zip(
                        categories.breadcrumb(prod.getCategoryId()),
                        sellers.getById(prod.getSellerId()),
                        reviews.list(productId),
                        qa.listByProduct(productId)
                ).map(t -> {
                    List<CategoryResponse.BreadcrumbNode> breadcrumb = t.getT1();
                    SellerResponse seller = t.getT2();
                    List<ReviewResponse> reviewSummary = t.getT3();
                    List<QaResponse> qaList = t.getT4();

                    ItemBasicResponse basic = ItemBasicResponse.from(prod, breadcrumb);
                    return ItemEnrichedResponse.builder()
                            .basic(basic)
                            .seller(seller)
                            .reviews(reviewSummary)
                            .qa(qaList)
                            .build();
                })
        );
    }
}