package com.mercadolibre.camilo.review.service;

import com.mercadolibre.camilo.review.dto.ReviewResponse;
import com.mercadolibre.camilo.review.dto.ReviewSummaryResponse;
import com.mercadolibre.camilo.review.model.Review;
import com.mercadolibre.camilo.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repo;

    public Flux<ReviewResponse> findByProduct(String productId) {
        return Flux.fromIterable(repo.findByProductId(productId))
                .map(ReviewResponse::from);
    }

    public Mono<ReviewSummaryResponse> summary(String productId) {
        List<Review> list = repo.findByProductId(productId);
        long count = list.size();
        double avg = count == 0 ? 0.0 :
                list.stream().mapToInt(Review::getRating).average().orElse(0.0);

        Map<Integer, Long> hist = new LinkedHashMap<>();
        for (int r = 1; r <= 5; r++) {
            int finalR = r;
            long c = list.stream().filter(rv -> rv.getRating() == finalR).count();
            hist.put(r, c);
        }

        return Mono.just(ReviewSummaryResponse.builder()
                .productId(productId)
                .count(count)
                .avg(avg)
                .histogram(hist)
                .build());
    }
}