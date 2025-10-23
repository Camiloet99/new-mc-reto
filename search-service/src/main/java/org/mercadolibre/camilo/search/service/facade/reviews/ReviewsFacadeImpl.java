package org.mercadolibre.camilo.search.service.facade.reviews;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.service.facade.ReviewsFacade;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewResponse;
import org.mercadolibre.camilo.search.exception.ReviewsInvalidRequestException;
import org.mercadolibre.camilo.search.exception.ReviewsNotFoundException;
import org.mercadolibre.camilo.search.exception.ReviewsUpstreamFailureException;
import org.mercadolibre.camilo.search.util.WebClientSupport;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewsFacadeImpl implements ReviewsFacade {

    private static final String LIST_TMPL = "/reviews?productId={pid}";

    private final WebClient webClient;
    private final EnvironmentConfig env;

    public Mono<List<ReviewResponse>> list(String productId) {
        final String pid = Objects.requireNonNull(productId, "productId must not be null").trim();
        final String base = env.getDomains().getReviewsBaseUrl().replaceAll("/$", "");
        final String urlTmpl = base + LIST_TMPL;
        final String pretty = urlTmpl.replace("{pid}", pid);

        return webClient.get()
                .uri(urlTmpl, pid)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp,
                        ReviewResponse[].class,
                        ctx -> new ReviewsNotFoundException(pretty, ctx.headers(), ctx.body()),
                        ctx -> new ReviewsInvalidRequestException(pretty, ctx.headers(), ctx.body()),
                        ctx -> new ReviewsUpstreamFailureException(ctx.status(), pretty, ctx.headers(), ctx.body())
                ))
                .flatMapMany(arr -> arr == null ? Flux.empty() : Flux.fromArray(arr))
                .doOnError(e -> log.error("Call to {} failed: {}", base, e.getMessage(), e))
                .collectList()
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(ReviewsUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}