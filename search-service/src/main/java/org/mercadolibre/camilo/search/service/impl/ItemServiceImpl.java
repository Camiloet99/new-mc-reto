package org.mercadolibre.camilo.search.service.impl;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import org.mercadolibre.camilo.search.service.ItemService;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewSummaryResponse;
import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.mercadolibre.camilo.search.service.facade.categories.CategoriesFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.products.ProductsFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.qa.QaFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.reviews.ReviewsFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.seller.SellersFacadeImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ProductsFacadeImpl products;
    private final CategoriesFacadeImpl categories;
    private final SellersFacadeImpl sellers;
    private final ReviewsFacadeImpl reviews;
    private final QaFacadeImpl qa;

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
                        reviews.summary(productId),
                        qa.listByProduct(productId)
                ).map(t -> {
                    List<CategoryResponse.BreadcrumbNode> breadcrumb = t.getT1();
                    SellerResponse seller = t.getT2();
                    ReviewSummaryResponse reviewSummary = t.getT3();
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