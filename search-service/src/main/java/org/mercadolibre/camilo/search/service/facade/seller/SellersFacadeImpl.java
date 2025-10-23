package org.mercadolibre.camilo.search.service.facade.seller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.service.facade.SellersFacade;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.mercadolibre.camilo.search.exception.SellersInvalidRequestException;
import org.mercadolibre.camilo.search.exception.SellersNotFoundException;
import org.mercadolibre.camilo.search.exception.SellersUpstreamFailureException;
import org.mercadolibre.camilo.search.util.WebClientSupport;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellersFacadeImpl implements SellersFacade {

    private static final String GET_BY_ID = "/sellers/%s";

    private final WebClient webClient;
    private final EnvironmentConfig env;

    public Mono<SellerResponse> getById(String sellerId) {
        final String base = env.getDomains().getSellersBaseUrl().replaceAll("/$", "");
        final String resourceUri = base + GET_BY_ID;

        return webClient.get()
                .uri(String.format(resourceUri, sellerId))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp, SellerResponse.class,
                        ctx -> new SellersNotFoundException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new SellersInvalidRequestException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new SellersUpstreamFailureException(ctx.status(), resourceUri, ctx.headers(), ctx.body())
                ))
                .doOnError(e -> log.error("Call to {} failed: {}", env.getDomains().getSellersBaseUrl(), e.getMessage(), e))
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(SellersUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}
