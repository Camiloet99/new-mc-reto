package org.camilo.mercadolibre.search.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.service.facade.*;
import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewResponse;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.mercadolibre.camilo.search.service.impl.ItemServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ProductsFacade products;
    @Mock
    private CategoriesFacade categories;
    @Mock
    private SellersFacade sellers;
    @Mock
    private ReviewsFacade reviews;
    @Mock
    private QaFacade qa;

    @InjectMocks
    private ItemServiceImpl service;

    private static final String PRODUCT_ID = "P-123";
    private static final String CATEGORY_ID = "C-9";
    private static final String SELLER_ID = "S-77";

    private ProductResponse product() {
        return ProductResponse.builder()
                .id(PRODUCT_ID)
                .pictures(Collections.emptyList())
                .attributes(Collections.emptyList())
                .condition("new")
                .hasPromotion(false)
                .price(BigDecimal.valueOf(100.0))
                .currency("USD")
                .stock(50)
                .thumbnail("http://example.com/image.jpg")
                .categoryId(CATEGORY_ID)
                .sellerId(SELLER_ID)
                .title("Demo product")
                .build();
    }

    private List<CategoryResponse.BreadcrumbNode> breadcrumb() {
        return List.of(
                CategoryResponse.BreadcrumbNode.builder().id("root").name("Root").build(),
                CategoryResponse.BreadcrumbNode.builder().id(CATEGORY_ID).name("Target").build()
        );
    }

    private SellerResponse seller() {
        return SellerResponse.builder()
                .id(SELLER_ID)
                .nickname("Demo Seller")
                .build();
    }

    private List<ReviewResponse> reviewSummary() {
        return List.of(
                ReviewResponse.builder().id("r1").productId(PRODUCT_ID).rating(5).createdAt(Instant.now().toString()).build(),
                ReviewResponse.builder().id("r2").productId(PRODUCT_ID).rating(4).createdAt(Instant.now().toString()).build()
        );
    }

    private List<QaResponse> qaList() {
        return List.of(
                QaResponse.builder().id("q1").productId(PRODUCT_ID).text("¿Es original?").author("Camilo").build()
        );
    }

    @Nested
    @DisplayName("basic(productId)")
    class Basic {

        @Test
        @DisplayName("devuelve ItemBasicResponse cuando products y categories responden OK")
        void basic_ok() {
            when(products.getById(PRODUCT_ID)).thenReturn(Mono.just(product()));
            when(categories.breadcrumb(CATEGORY_ID)).thenReturn(Mono.just(breadcrumb()));

            StepVerifier.create(service.basic(PRODUCT_ID))
                    .expectNextMatches(basic -> {
                        return basic != null
                                && PRODUCT_ID.equals(basic.getId())
                                && basic.getCategoryBreadcrumb() != null
                                && basic.getCategoryBreadcrumb().size() == 2;
                    })
                    .verifyComplete();

            verify(products).getById(PRODUCT_ID);
            verify(categories).breadcrumb(CATEGORY_ID);
            verifyNoMoreInteractions(products, categories, sellers, reviews, qa);
        }

        @Test
        @DisplayName("propaga error si falla products.getById")
        void basic_errors_when_products_fails() {
            RuntimeException boom = new RuntimeException("upstream failure");
            when(products.getById(PRODUCT_ID)).thenReturn(Mono.error(boom));

            StepVerifier.create(service.basic(PRODUCT_ID))
                    .expectErrorMatches(ex -> ex == boom)
                    .verify();

            verify(products).getById(PRODUCT_ID);
            verifyNoInteractions(categories, sellers, reviews, qa);
        }
    }

    @Nested
    @DisplayName("enriched(productId)")
    class Enriched {

        @Test
        @DisplayName("devuelve ItemEnrichedResponse completo cuando todas las dependencias responden OK")
        void enriched_ok() {
            when(products.getById(PRODUCT_ID)).thenReturn(Mono.just(product()));
            when(categories.breadcrumb(CATEGORY_ID)).thenReturn(Mono.just(breadcrumb()));
            when(sellers.getById(SELLER_ID)).thenReturn(Mono.just(seller()));
            when(reviews.list(PRODUCT_ID)).thenReturn(Mono.just(reviewSummary()));
            when(qa.listByProduct(PRODUCT_ID)).thenReturn(Mono.just(qaList()));

            StepVerifier.create(service.enriched(PRODUCT_ID))
                    .expectNextMatches(enriched -> {
                        ItemBasicResponse basic = enriched.getBasic();
                        return basic != null
                                && PRODUCT_ID.equals(basic.getId())
                                && enriched.getSeller() != null
                                && SELLER_ID.equals(enriched.getSeller().getId())
                                && enriched.getReviews().size() == 2
                                && enriched.getQa().size() == 1;
                    })
                    .verifyComplete();

            verify(products).getById(PRODUCT_ID);
            verify(categories).breadcrumb(CATEGORY_ID);
            verify(sellers).getById(SELLER_ID);
            verify(reviews).list(PRODUCT_ID);
            verify(qa).listByProduct(PRODUCT_ID);
            verifyNoMoreInteractions(products, categories, sellers, reviews, qa);
        }

        @Test
        @DisplayName("aplica fallbacks: breadcrumb/seller/reviews/qa errores -> valores vacíos por onErrorReturn")
        void enriched_fallbacks_when_dependents_fail() {
            when(products.getById(PRODUCT_ID)).thenReturn(Mono.just(product()));
            when(categories.breadcrumb(CATEGORY_ID)).thenReturn(Mono.error(new RuntimeException("cat fail")));
            when(sellers.getById(SELLER_ID)).thenReturn(Mono.error(new RuntimeException("seller fail")));
            when(reviews.list(PRODUCT_ID)).thenReturn(Mono.error(new RuntimeException("reviews fail")));
            when(qa.listByProduct(PRODUCT_ID)).thenReturn(Mono.error(new RuntimeException("qa fail")));

            StepVerifier.create(service.enriched(PRODUCT_ID))
                    .expectNextMatches(enriched -> {
                        boolean bcOk = enriched.getBasic() != null
                                && enriched.getBasic().getCategoryBreadcrumb() != null
                                && enriched.getBasic().getCategoryBreadcrumb().isEmpty();
                        boolean sellerOk = enriched.getSeller() != null
                                && enriched.getSeller().getId() == null;
                        boolean reviewsOk = enriched.getReviews() != null && enriched.getReviews().isEmpty();
                        boolean qaOk = enriched.getQa() != null && enriched.getQa().isEmpty();
                        return bcOk && sellerOk && reviewsOk && qaOk;
                    })
                    .verifyComplete();

            verify(products).getById(PRODUCT_ID);
            verify(categories).breadcrumb(CATEGORY_ID);
            verify(sellers).getById(SELLER_ID);
            verify(reviews).list(PRODUCT_ID);
            verify(qa).listByProduct(PRODUCT_ID);
            verifyNoMoreInteractions(products, categories, sellers, reviews, qa);
        }

        @Test
        @DisplayName("propaga error si falla products.getById (nada más se invoca)")
        void enriched_errors_when_products_fails() {
            when(products.getById(PRODUCT_ID)).thenReturn(Mono.error(new IllegalStateException("prod fail")));

            StepVerifier.create(service.enriched(PRODUCT_ID))
                    .expectErrorSatisfies(ex -> {
                        assert ex instanceof IllegalStateException;
                    })
                    .verify();

            verify(products).getById(PRODUCT_ID);
            verifyNoInteractions(categories, sellers, reviews, qa);
        }
    }
}
