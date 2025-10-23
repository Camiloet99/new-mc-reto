package org.mercadolibre.camilo.review.service.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mercadolibre.camilo.review.dto.ReviewResponse;
import org.mercadolibre.camilo.review.dto.ReviewSummaryResponse;
import org.mercadolibre.camilo.review.exception.InvalidRequestException;
import org.mercadolibre.camilo.review.model.Review;
import org.mercadolibre.camilo.review.repository.ReviewRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    ReviewRepository repo;

    @InjectMocks
    ReviewServiceImpl service;

    private Review review(String id, String productId, int rating) {
        return Review.builder()
                .id(id)
                .productId(productId)
                .rating(rating)
                .title("Titulo " + id)
                .text("Texto reseña " + id)
                .createdAt("2025-01-01T00:00:00Z")
                .author("autor-" + id)
                .build();
    }

    @Test
    @DisplayName("findByProduct debe fallar si productId es null")
    void findByProduct_null_shouldError() {
        StepVerifier.create(service.findByProduct(null))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(InvalidRequestException.class)
                        .hasMessage("productId must not be blank"))
                .verify();
    }

    @Test
    @DisplayName("findByProduct debe fallar si productId es blanco")
    void findByProduct_blank_shouldError() {
        StepVerifier.create(service.findByProduct(" \t"))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(InvalidRequestException.class)
                        .hasMessage("productId must not be blank"))
                .verify();
    }

    @Test
    @DisplayName("findByProduct debe retornar lista vacía si repo devuelve null")
    void findByProduct_repoReturnsNull_shouldReturnEmptyFlux() {
        String productId = "PNULL";
        when(repo.findByProductId(productId)).thenReturn(null);

        StepVerifier.create(service.findByProduct(productId))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByProduct mapea correctamente las reseñas a ReviewResponse")
    void findByProduct_ok_shouldMap() {
        String productId = "P1";
        List<Review> list = List.of(
                review("r1", productId, 5),
                review("r2", productId, 3),
                review("r3", productId, 1)
        );
        when(repo.findByProductId(productId)).thenReturn(list);

        StepVerifier.create(service.findByProduct(productId).collectList())
                .assertNext(responses -> {
                    assertThat(responses).hasSize(3);
                    assertThat(responses.stream().map(ReviewResponse::getId)).containsExactly("r1", "r2", "r3");
                    assertThat(responses.stream().map(ReviewResponse::getRating)).containsExactly(5, 3, 1);
                    assertThat(responses.stream().map(ReviewResponse::getProductId)).allMatch(pid -> pid.equals(productId));
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("summary debe fallar si productId es null")
    void summary_null_shouldError() {
        StepVerifier.create(service.summary(null))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(InvalidRequestException.class)
                        .hasMessage("productId must not be blank"))
                .verify();
    }

    @Test
    @DisplayName("summary debe fallar si productId es blanco")
    void summary_blank_shouldError() {
        StepVerifier.create(service.summary("    "))
                .expectErrorSatisfies(ex -> assertThat(ex)
                        .isInstanceOf(InvalidRequestException.class)
                        .hasMessage("productId must not be blank"))
                .verify();
    }

    @Test
    @DisplayName("summary retorna métricas vacías si repo devuelve lista vacía")
    void summary_emptyList_shouldReturnZeros() {
        String productId = "PEMPTY";
        when(repo.findByProductId(productId)).thenReturn(List.of());

        StepVerifier.create(service.summary(productId))
                .assertNext(summary -> {
                    assertThat(summary.getProductId()).isEqualTo(productId);
                    assertThat(summary.getCount()).isEqualTo(0);
                    assertThat(summary.getAvg()).isEqualTo(0.0);
                    assertHistogram(summary.getHistogram(), Map.of(1L,0L));
                    assertThat(summary.getHistogram().values()).allMatch(v -> v == 0L);
                    assertThat(summary.getHistogram().keySet()).containsExactly(1,2,3,4,5);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("summary retorna métricas correctas con distribución de ratings")
    void summary_ok_shouldComputeMetrics() {
        String productId = "P1";
        List<Review> list = List.of(
                review("r1", productId, 5),
                review("r2", productId, 5),
                review("r3", productId, 3),
                review("r4", productId, 1)
        );
        when(repo.findByProductId(productId)).thenReturn(list);

        StepVerifier.create(service.summary(productId))
                .assertNext(summary -> {
                    assertThat(summary.getProductId()).isEqualTo(productId);
                    assertThat(summary.getCount()).isEqualTo(4);
                    assertThat(summary.getAvg()).isEqualTo(3.5);
                    Map<Integer, Long> expectedHistogram = new LinkedHashMap<>();
                    expectedHistogram.put(1, 1L);
                    expectedHistogram.put(2, 0L);
                    expectedHistogram.put(3, 1L);
                    expectedHistogram.put(4, 0L);
                    expectedHistogram.put(5, 2L);
                    assertThat(summary.getHistogram()).containsExactlyEntriesOf(expectedHistogram);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("summary retorna métricas vacías si repo devuelve null")
    void summary_repoReturnsNull_shouldReturnZeros() {
        String productId = "PNULL";
        when(repo.findByProductId(productId)).thenReturn(null);

        StepVerifier.create(service.summary(productId))
                .assertNext(summary -> {
                    assertThat(summary.getProductId()).isEqualTo(productId);
                    assertThat(summary.getCount()).isEqualTo(0);
                    assertThat(summary.getAvg()).isEqualTo(0.0);
                    assertThat(summary.getHistogram().keySet()).containsExactly(1,2,3,4,5);
                    assertThat(summary.getHistogram().values()).allMatch(v -> v == 0L);
                })
                .verifyComplete();
    }

    private void assertHistogram(Map<Integer, Long> histogram, Map<Long, Long> dummy) {
        assertThat(histogram).hasSize(5);
        assertThat(histogram.keySet()).containsExactly(1,2,3,4,5);
    }
}

