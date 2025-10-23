package org.mercadolibre.camilo.search.service.facade.categories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoriesFacadeImpl implements CategoriesFacade {

    private final WebClient webClient;
    private final EnvironmentConfig env;

    private static final String BREADCRUMB_TMPL = "/categories/{id}/breadcrumb";

    @Override
    public Mono<List<CategoryResponse.BreadcrumbNode>> breadcrumb(String categoryId) {
        final String id = Objects.requireNonNull(categoryId, "categoryId must not be null").trim();
        final String base = env.getDomains().getCategoriesBaseUrl();
        final String urlTmpl = base.replaceAll("/$", "") + BREADCRUMB_TMPL;

        return webClient.get()
                .uri(urlTmpl, id)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(resp -> WebClientSupport.mapResponse(
                        resp, CategoryResponse.BreadcrumbNode[].class,
                        ctx -> new CategoriesNotFoundException(urlTmpl, ctx.headers(), ctx.body()),
                        ctx -> new CategoriesInvalidRequestException(urlTmpl, ctx.headers(), ctx.body()),
                        ctx -> new CategoriesUpstreamFailureException(ctx.status(), urlTmpl, ctx.headers(), ctx.body())
                ))
                .map(Arrays::asList)
                .doOnError(e -> log.error("Categories.breadcrumb failed url={} msg={}", urlTmpl.replace("{id}", id), e.getMessage(), e))
                .retryWhen(Retry
                        .max(env.getServiceRetry().getMaxAttempts())
                        .filter(CategoriesUpstreamFailureException.class::isInstance)
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }
}