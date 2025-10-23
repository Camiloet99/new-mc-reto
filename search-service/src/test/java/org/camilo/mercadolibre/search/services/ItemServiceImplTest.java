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

    @Nested
    @DisplayName("enrichedPage(categoryId,sellerId,q,page,elements)")
    class EnrichedPage {

        private ProductResponse product(String id, String category, String seller, String title) {
            return ProductResponse.builder()
                    .id(id)
                    .pictures(Collections.emptyList())
                    .attributes(Collections.emptyList())
                    .condition("new")
                    .hasPromotion(false)
                    .price(BigDecimal.valueOf(10.0))
                    .currency("USD")
                    .stock(5)
                    .thumbnail("http://example.com/" + id + ".jpg")
                    .categoryId(category)
                    .sellerId(seller)
                    .title(title)
                    .build();
        }

        @Test
        @DisplayName("retorna página enriquecida con un elemento (happy path)")
        void enrichedPage_single_ok() {
            ProductResponse p1 = product("P1", "C1", "S1", "Prod 1");
            when(products.getAll(null, null, "term", 0, 1)).thenReturn(Mono.just(
                    org.mercadolibre.camilo.search.dto.PageResponse.<ProductResponse>builder()
                            .page(0).size(1).totalItems(1).totalPages(1).hasPrev(false).hasNext(false)
                            .item(p1)
                            .build()
            ));
            when(categories.breadcrumb("C1")).thenReturn(Mono.just(List.of()));
            when(sellers.getById("S1")).thenReturn(Mono.just(SellerResponse.builder().id("S1").nickname("Seller 1").build()));
            when(reviews.list("P1")).thenReturn(Mono.just(List.of(ReviewResponse.builder().id("r1").productId("P1").rating(5).createdAt(Instant.now().toString()).build())));
            when(qa.listByProduct("P1")).thenReturn(Mono.just(List.of(QaResponse.builder().id("q1").productId("P1").text("hola").author("U").build())));

            StepVerifier.create(service.enrichedPage(null, null, "term", 0, 1))
                    .expectNextMatches(page -> page.getItems().size() == 1 && page.getTotalItems() == 1 && page.getItems().get(0).getBasic().getId().equals("P1"))
                    .verifyComplete();

            verify(products).getAll(null, null, "term", 0, 1);
            verify(categories).breadcrumb("C1");
            verify(sellers).getById("S1");
            verify(reviews).list("P1");
            verify(qa).listByProduct("P1");
            verifyNoMoreInteractions(products, categories, sellers, reviews, qa);
        }

        @Test
        @DisplayName("múltiples productos: algunos enriquecen OK y otros usan fallbacks por errores")
        void enrichedPage_multiple_with_fallbacks() {
            ProductResponse ok = product("P10", "C10", "S10", "OK");
            ProductResponse fail = product("P11", "C11", "S11", "FAIL");
            when(products.getAll("cat", null, null, 1, 2)).thenReturn(Mono.just(
                    org.mercadolibre.camilo.search.dto.PageResponse.<ProductResponse>builder()
                            .page(1).size(2).totalItems(2).totalPages(1).hasPrev(true).hasNext(false)
                            .item(ok).item(fail)
                            .build()
            ));
            when(categories.breadcrumb("C10")).thenReturn(Mono.just(List.of(CategoryResponse.BreadcrumbNode.builder().id("root").name("Root").build())));
            when(sellers.getById("S10")).thenReturn(Mono.just(SellerResponse.builder().id("S10").nickname("Seller10").build()));
            when(reviews.list("P10")).thenReturn(Mono.just(List.of()));
            when(qa.listByProduct("P10")).thenReturn(Mono.just(List.of()));
            when(categories.breadcrumb("C11")).thenReturn(Mono.error(new RuntimeException("cat fail")));
            when(sellers.getById("S11")).thenReturn(Mono.error(new RuntimeException("seller fail")));
            when(reviews.list("P11")).thenReturn(Mono.error(new RuntimeException("reviews fail")));
            when(qa.listByProduct("P11")).thenReturn(Mono.error(new RuntimeException("qa fail")));

            StepVerifier.create(service.enrichedPage("cat", null, null, 1, 2))
                    .expectNextMatches(page -> {
                        return page.getItems().size() == 2 &&
                                page.getItems().stream().anyMatch(e -> e.getBasic().getId().equals("P10") && e.getSeller().getId().equals("S10")) &&
                                page.getItems().stream().anyMatch(e -> e.getBasic().getId().equals("P11") && e.getSeller().getId() == null && e.getReviews().isEmpty());
                    })
                    .verifyComplete();

            verify(products).getAll("cat", null, null, 1, 2);
            verify(categories).breadcrumb("C10");
            verify(categories).breadcrumb("C11");
            verify(sellers).getById("S10");
            verify(sellers).getById("S11");
            verify(reviews).list("P10");
            verify(reviews).list("P11");
            verify(qa).listByProduct("P10");
            verify(qa).listByProduct("P11");
            verifyNoMoreInteractions(products, categories, sellers, reviews, qa);
        }

        @Test
        @DisplayName("propaga error si falla products.getAll")
        void enrichedPage_products_getAll_error() {
            when(products.getAll(any(), any(), any(), any(), any())).thenReturn(Mono.error(new IllegalStateException("getAll fail")));

            StepVerifier.create(service.enrichedPage("c", "s", "q", 0, 10))
                    .expectError(IllegalStateException.class)
                    .verify();

            verify(products).getAll("c", "s", "q", 0, 10);
            verifyNoInteractions(categories, sellers, reviews, qa);
        }

        @Test
        @DisplayName("página vacía -> items vacíos sin llamadas a dependencias de enriquecimiento")
        void enrichedPage_empty_items() {
            when(products.getAll(null, null, null, 0, 0)).thenReturn(Mono.just(
                    org.mercadolibre.camilo.search.dto.PageResponse.<ProductResponse>builder()
                            .page(0).size(0).totalItems(0).totalPages(0).hasPrev(false).hasNext(false)
                            .build()
            ));

            StepVerifier.create(service.enrichedPage(null, null, null, 0, 0))
                    .expectNextMatches(page -> page.getItems().isEmpty() && page.getTotalItems() == 0)
                    .verifyComplete();

            verify(products).getAll(null, null, null, 0, 0);
            verifyNoInteractions(categories, sellers, reviews, qa);
        }
    }
}
