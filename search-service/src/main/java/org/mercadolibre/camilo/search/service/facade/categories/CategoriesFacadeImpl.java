package org.mercadolibre.camilo.search.service.facade.categories;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.service.facade.CategoriesFacade;
import org.mercadolibre.camilo.search.service.facade.categories.model.CategoryResponse;
import org.mercadolibre.camilo.search.exception.CategoriesInvalidRequestException;
import org.mercadolibre.camilo.search.exception.CategoriesNotFoundException;
import org.mercadolibre.camilo.search.exception.CategoriesUpstreamFailureException;
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
public class CategoriesFacadeImpl implements CategoriesFacade {

    private static final String BREADCRUMB = "/categories/{id}/breadcrumb";
    private final WebClient webClient;
    private final EnvironmentConfig env;

    public Mono<List<CategoryResponse.BreadcrumbNode>> breadcrumb(String categoryId) {
        final String base = env.getDomains().getCategoriesBaseUrl().replaceAll("/$", "");
        final String resourceUri = base + BREADCRUMB;

        return webClient.get()
                .uri(resourceUri, categoryId)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp, CategoryResponse.BreadcrumbNode[].class,
                        ctx -> new CategoriesNotFoundException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new CategoriesInvalidRequestException(resourceUri, ctx.headers(), ctx.body()),
                        ctx -> new CategoriesUpstreamFailureException(ctx.status(), resourceUri, ctx.headers(), ctx.body())
                )).map(Arrays::asList)
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(CategoriesUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}