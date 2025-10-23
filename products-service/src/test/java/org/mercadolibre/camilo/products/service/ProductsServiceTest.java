package org.mercadolibre.camilo.products.service;

import org.mercadolibre.camilo.products.dto.ProductResponse;
import org.mercadolibre.camilo.products.exception.InvalidRequestException;
import org.mercadolibre.camilo.products.exception.ProductNotFoundException;
import org.mercadolibre.camilo.products.model.Product;
import org.mercadolibre.camilo.products.repository.impl.ProductRepositoryImpl;
import org.mercadolibre.camilo.products.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    ProductRepositoryImpl repo;

    @InjectMocks
    ProductServiceImpl serviceImpl;

    ProductService service;

    Product p1, p2, p3;

    @BeforeEach
    void setUp() {
        service = serviceImpl;

        p1 = product("P-1", "iPhone 15", "C-1", "S-1");
        p2 = product("P-2", "Xiaomi Redmi", "C-1", "S-2");
        p3 = product("P-3", "Parlante Bluetooth", "C-2", "S-1");
    }

    @Test
    @DisplayName("get(null) -> InvalidRequestException")
    void get_null_throws() {
        StepVerifier.create(service.get(null))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(InvalidRequestException.class);
                    assertThat(err).hasMessage("Product id must not be blank");
                })
                .verify();
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("get(blank) -> InvalidRequestException")
    void get_blank_throws() {
        StepVerifier.create(service.get("   "))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(InvalidRequestException.class);
                    assertThat(err).hasMessage("Product id must not be blank");
                })
                .verify();
        verifyNoInteractions(repo);
    }

    @Test
    @DisplayName("get(not found) -> ProductNotFoundException")
    void get_notFound_throws() {
        when(repo.findById("P-404")).thenReturn(Optional.empty());

        StepVerifier.create(service.get("P-404"))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(ProductNotFoundException.class);
                    assertThat(err.getMessage()).isEqualTo("Product 'P-404' not found");
                })
                .verify();

        verify(repo, times(1)).findById("P-404");
        verifyNoMoreInteractions(repo);
    }

    @Test
    @DisplayName("get(ok) -> mapea a ProductResponse")
    void get_ok_maps() {
        when(repo.findById("P-1")).thenReturn(Optional.of(p1));

        StepVerifier.create(service.get("P-1"))
                .assertNext(resp -> {
                    assertThat(resp.getId()).isEqualTo("P-1");
                    assertThat(resp.getTitle()).isEqualTo("iPhone 15");
                })
                .verifyComplete();

        verify(repo, times(1)).findById("P-1");
        verifyNoMoreInteractions(repo);
    }

    /**
     * Crea un Product "completo" con campos no nulos
     * que suelen requerir ProductResponse.from(...).
     * Ajusta los defaults si tu modelo exige otros campos.
     */
    private Product product(String id, String title, String categoryId, String sellerId) {
        return Product.builder()
                .id(id).title(title)
                .categoryId(categoryId).sellerId(sellerId)
                .price(new BigDecimal("100.00")).currency("USD")
                .thumbnail("thumb.jpg").pictures(emptyList())
                .attributes(emptyList())
                .condition("new").description("...")
                .stock(10).hasPromotion(false)
                .build();
    }
}