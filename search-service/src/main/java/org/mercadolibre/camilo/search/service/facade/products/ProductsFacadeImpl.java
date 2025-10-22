package org.mercadolibre.camilo.search.service.facade.products;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.service.facade.ProductsFacade;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;
import org.mercadolibre.camilo.search.exception.ProductsInvalidRequestException;
import org.mercadolibre.camilo.search.exception.ProductsNotFoundException;
import org.mercadolibre.camilo.search.exception.ProductsUpstreamFailureException;
import org.mercadolibre.camilo.search.util.WebClientSupport;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class ProductsFacadeImpl implements ProductsFacade {

    private static final String GET_BY_ID = "/products/{id}";

    private final WebClient webClient;
    private final EnvironmentConfig env;

    public Mono<ProductResponse> getById(String productId) {
        final String base = env.getDomains().getProductsBaseUrl().replaceAll("/$", "");
        final String resourceUri = base + GET_BY_ID;

        return webClient.get()
                .uri(resourceUri, productId)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp, ProductResponse.class,
                        ctx -> new ProductsNotFoundException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new ProductsInvalidRequestException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new ProductsUpstreamFailureException(ctx.status(), resourceUri, ctx.headers(), ctx.body())
                ))
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(ProductsUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}
