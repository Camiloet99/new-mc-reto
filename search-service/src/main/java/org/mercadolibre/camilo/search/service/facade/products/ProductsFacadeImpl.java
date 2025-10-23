package org.mercadolibre.camilo.search.service.facade.products;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.dto.PageResponse;
import org.mercadolibre.camilo.search.service.facade.ProductsFacade;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;
import org.mercadolibre.camilo.search.exception.ProductsInvalidRequestException;
import org.mercadolibre.camilo.search.exception.ProductsNotFoundException;
import org.mercadolibre.camilo.search.exception.ProductsUpstreamFailureException;
import org.mercadolibre.camilo.search.util.WebClientSupport;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductsFacadeImpl implements ProductsFacade {

    private static final String GET_BY_ID = "/products/%s";
    private static final String GET_ALL = "/products";

    private final WebClient webClient;
    private final EnvironmentConfig env;

    @Override
    public Mono<ProductResponse> getById(String productId) {
        final String base = env.getDomains().getProductsBaseUrl().replaceAll("/$", "");
        final String resourceUri = base + GET_BY_ID;

        return webClient.get()
                .uri(String.format(resourceUri, productId))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp, ProductResponse.class,
                        ctx -> new ProductsNotFoundException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new ProductsInvalidRequestException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new ProductsUpstreamFailureException(ctx.status(), resourceUri, ctx.headers(), ctx.body())
                ))
                .doOnError(e -> log.error("Call to {} failed: {}", env.getDomains().getProductsBaseUrl(), e.getMessage(), e))
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(ProductsUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    @Override
    public Mono<PageResponse<ProductResponse>> getAll(
            String categoryId,
            String sellerId,
            String q,
            Integer page,
            Integer elements
    ) {
        final String base = env.getDomains().getProductsBaseUrl().replaceAll("/$", "");
        final String resourceUri = base + GET_ALL;

        return webClient.get()
                .uri(buildProductsListUrl(base, categoryId, sellerId, q, page, elements))
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> {
                    final HttpStatusCode status = resp.statusCode();
                    if (status.is2xxSuccessful()) {
                        return resp.bodyToMono(new ParameterizedTypeReference<PageResponse<ProductResponse>>() {
                        });
                    }
                    return resp.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                HttpHeaders headers = resp.headers().asHttpHeaders();
                                if (status.is4xxClientError()) {
                                    return Mono.error(new ProductsInvalidRequestException(resourceUri, headers, body));
                                }
                                return Mono.error(new ProductsUpstreamFailureException(status.value(), resourceUri, headers, body));
                            });
                })
                .doOnError(e -> log.error("Call to {} failed: {}", env.getDomains().getProductsBaseUrl(), e.getMessage(), e))
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(ProductsUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private String buildProductsListUrl(
            String base,
            String categoryId,
            String sellerId,
            String q,
            Integer page,
            Integer elements
    ) {
        final String normalizedBase = base.replaceAll("/$", "");
        final String resourceUri = normalizedBase + GET_ALL;

        UriComponentsBuilder componentsBuilder = UriComponentsBuilder.fromHttpUrl(resourceUri);

        if (categoryId != null && !categoryId.isBlank()) componentsBuilder.queryParam("categoryId", categoryId);
        if (sellerId != null && !sellerId.isBlank()) componentsBuilder.queryParam("sellerId", sellerId);
        if (q != null && !q.isBlank()) componentsBuilder.queryParam("q", q);
        if (page != null) componentsBuilder.queryParam("page", page);
        if (elements != null) componentsBuilder.queryParam("elements", elements);

        return componentsBuilder.build(true).toUriString();
    }
}