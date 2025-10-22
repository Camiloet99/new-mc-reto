package org.mercadolibre.camilo.search.service.facade.qa;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.service.facade.QaFacade;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.mercadolibre.camilo.search.exception.QaInvalidRequestException;
import org.mercadolibre.camilo.search.exception.QaNotFoundException;
import org.mercadolibre.camilo.search.exception.QaUpstreamFailureException;
import org.mercadolibre.camilo.search.util.WebClientSupport;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class QaFacadeImpl implements QaFacade {

    private static final String LIST = "/qa";

    private final WebClient webClient;
    private final EnvironmentConfig env;

    public Mono<List<QaResponse>> listByProduct(String productId) {
        final String base = env.getDomains().getQaBaseUrl().replaceAll("/$", "");
        final String resourceUri = base + LIST;

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(resourceUri).queryParam("productId", productId).build())
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp, QaResponse[].class,
                        ctx -> new QaNotFoundException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new QaInvalidRequestException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new QaUpstreamFailureException(ctx.status(), resourceUri, ctx.headers(), ctx.body())
                )).map(Arrays::asList)
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(QaUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}