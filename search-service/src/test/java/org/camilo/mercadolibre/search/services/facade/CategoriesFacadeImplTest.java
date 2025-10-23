package org.camilo.mercadolibre.search.services.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.exception.CategoriesInvalidRequestException;
import org.mercadolibre.camilo.search.exception.CategoriesNotFoundException;
import org.mercadolibre.camilo.search.exception.CategoriesUpstreamFailureException;
import org.mercadolibre.camilo.search.service.facade.categories.CategoriesFacadeImpl;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

class CategoriesFacadeImplTest {

    private DisposableServer server;

    private EnvironmentConfig buildEnv(String baseUrl, int maxRetries) {
        EnvironmentConfig env = new EnvironmentConfig();
        env.setServiceName("search-service");
        env.setMaxPayloadSizeInMb(5);
        env.setSecurityDisableSslCertValidation(true);
        env.setLogInvalidRequests(true);
        EnvironmentConfig.ServiceRetry sr = new EnvironmentConfig.ServiceRetry();
        sr.setMaxAttempts(maxRetries);
        env.setServiceRetry(sr);
        EnvironmentConfig.Domains domains = new EnvironmentConfig.Domains();
        domains.setCategoriesBaseUrl(baseUrl);
        domains.setProductsBaseUrl(baseUrl);
        domains.setSellersBaseUrl(baseUrl);
        domains.setReviewsBaseUrl(baseUrl);
        domains.setQaBaseUrl(baseUrl);
        env.setDomains(domains);
        EnvironmentConfig.Http http = new EnvironmentConfig.Http();
        http.setTimeoutMs(1000L);
        env.setHttp(http);
        return env;
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @Test
    void breadcrumb_success_returnsList() {
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/categories/123/breadcrumb", (req, resp) ->
                        resp.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .sendString(Mono.just("[{\"id\":\"1\",\"name\":\"Root\"},{\"id\":\"2\",\"name\":\"Child\"}]"))))
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(baseUrl, 0);
        CategoriesFacadeImpl facade = new CategoriesFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.breadcrumb("123"))
                .expectNextMatches(list -> list.size() == 2 &&
                        list.get(0).getId().equals("1") && list.get(1).getName().equals("Child"))
                .verifyComplete();
    }

    @Test
    void breadcrumb_trimsInput_success() {
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/categories/123/breadcrumb", (req, resp) ->
                        resp.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                                .sendString(Mono.just("[{\"id\":\"1\",\"name\":\"Root\"}]"))))
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(baseUrl, 0);
        CategoriesFacadeImpl facade = new CategoriesFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.breadcrumb(" 123 "))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getId().equals("1"))
                .verifyComplete();
    }

    @Test
    void breadcrumb_notFound_throwsCategoriesNotFoundException() {
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/categories/999/breadcrumb", (req, resp) ->
                        resp.status(404).header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                .sendString(Mono.just("Not Found"))))
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(baseUrl, 1);
        CategoriesFacadeImpl facade = new CategoriesFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.breadcrumb("999"))
                .expectError(CategoriesNotFoundException.class)
                .verify();
    }

    @Test
    void breadcrumb_invalidRequest_400_throwsCategoriesInvalidRequestException() {
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/categories/bad-id/breadcrumb", (req, resp) ->
                        resp.status(400).header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                .sendString(Mono.just("Bad Request"))))
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(baseUrl, 1);
        CategoriesFacadeImpl facade = new CategoriesFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.breadcrumb("bad-id"))
                .expectError(CategoriesInvalidRequestException.class)
                .verify();
    }

    @Test
    void breadcrumb_upstreamFailure_retriesThenFailsAfterMaxRetries() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/categories/500/breadcrumb", (req, resp) -> {
                    counter.incrementAndGet();
                    return resp.status(500).header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                            .sendString(Mono.just("Internal Error"));
                }))
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        int maxRetries = 2;
        EnvironmentConfig env = buildEnv(baseUrl, maxRetries);
        CategoriesFacadeImpl facade = new CategoriesFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.breadcrumb("500"))
                .expectError(CategoriesUpstreamFailureException.class)
                .verify();

        int attempts = counter.get();
        if (attempts != (1 + maxRetries)) {
            throw new AssertionError("Expected " + (1 + maxRetries) + " attempts, got " + attempts);
        }
    }

    @Test
    void breadcrumb_upstreamFailure_thenSuccessBeforeExhaustingRetries() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create()
                .port(0)
                .route(routes -> routes.get("/categories/123/breadcrumb", (req, resp) -> {
                    int attempt = counter.incrementAndGet();
                    if (attempt < 2) {
                        return resp.status(500).header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                .sendString(Mono.just("Internal Error"));
                    }
                    return resp.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .sendString(Mono.just("[{\"id\":\"A\",\"name\":\"Root\"}]"));
                }))
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        int maxRetries = 3;
        EnvironmentConfig env = buildEnv(baseUrl, maxRetries);
        CategoriesFacadeImpl facade = new CategoriesFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.breadcrumb("123"))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getId().equals("A"))
                .verifyComplete();

        int attempts = counter.get();
        if (attempts != 2) {
            throw new AssertionError("Expected exactly 2 attempts (1 failure + 1 success), got " + attempts);
        }
    }
}
