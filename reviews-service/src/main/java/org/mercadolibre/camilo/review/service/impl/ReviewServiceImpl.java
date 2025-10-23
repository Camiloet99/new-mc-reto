package org.mercadolibre.camilo.review.service.impl;

import org.mercadolibre.camilo.review.dto.ReviewResponse;
import org.mercadolibre.camilo.review.dto.ReviewSummaryResponse;
import org.mercadolibre.camilo.review.exception.InvalidRequestException;
import org.mercadolibre.camilo.review.model.Review;
import org.mercadolibre.camilo.review.repository.ReviewRepository;
import org.mercadolibre.camilo.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository repo;

    @Override
    public Flux<ReviewResponse> findByProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            log.warn("ReviewService.findByProduct | invalid productId (blank)");
            return Flux.error(new InvalidRequestException("productId must not be blank"));
        }

        log.info("ReviewService.findByProduct | productId={}", productId);
        return Flux.fromIterable(safeList(repo.findByProductId(productId)))
                .map(ReviewResponse::from)
                .doOnComplete(() -> log.debug("ReviewService.findByProduct | completed | productId={}", productId))
                .doOnError(ex -> log.error("ReviewService.findByProduct | error | productId={} | type={} | msg={}",
                        productId, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @Override
    public Mono<ReviewSummaryResponse> summary(String productId) {
        if (productId == null || productId.isBlank()) {
            log.warn("ReviewService.summary | invalid productId (blank)");
            return Mono.error(new InvalidRequestException("productId must not be blank"));
        }

        log.info("ReviewService.summary | computing | productId={}", productId);
        return Mono.fromSupplier(() -> {
                    List<Review> list = safeList(repo.findByProductId(productId));
                    long count = list.size();
                    double avg = count == 0 ? 0.0 :
                            list.stream().collect(Collectors.averagingInt(Review::getRating));

                    // Histograma 1..5 preservando orden
                    Map<Integer, Long> histogram = IntStream.rangeClosed(1, 5)
                            .boxed()
                            .collect(Collectors.toMap(
                                    r -> r,
                                    r -> list.stream().filter(rv -> rv.getRating() == r).count(),
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

                    log.debug("ReviewService.summary | ok | productId={} count={} avg={}", productId, count, avg);

                    return ReviewSummaryResponse.builder()
                            .productId(productId)
                            .count(count)
                            .avg(avg)
                            .histogram(histogram)
                            .build();
                })
                .doOnError(ex -> log.error("ReviewService.summary | error | productId={} | type={} | msg={}",
                        productId, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    private static <T> List<T> safeList(List<T> in) {
        return in == null ? List.of() : in;
    }
}