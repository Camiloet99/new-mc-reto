package org.mercadolibre.camilo.search.service.facade.reviews;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.service.facade.ReviewsFacade;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewSummaryResponse;
import org.mercadolibre.camilo.search.exception.ReviewsInvalidRequestException;
import org.mercadolibre.camilo.search.exception.ReviewsNotFoundException;
import org.mercadolibre.camilo.search.exception.ReviewsUpstreamFailureException;
import org.mercadolibre.camilo.search.util.WebClientSupport;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class ReviewsFacadeImpl implements ReviewsFacade {

    private static final String SUMMARY = "/reviews/summary";

    private final WebClient webClient;
    private final EnvironmentConfig env;

    public Mono<ReviewSummaryResponse> summary(String productId) {
        final String base = env.getDomains().getReviewsBaseUrl().replaceAll("/$", "");
        final String resourceUri = base + SUMMARY;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(resourceUri).queryParam("productId", productId).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp, ReviewSummaryResponse.class,
                        ctx -> new ReviewsNotFoundException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new ReviewsInvalidRequestException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new ReviewsUpstreamFailureException(ctx.status(), resourceUri, ctx.headers(), ctx.body())
                ))
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(ReviewsUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}