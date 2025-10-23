package org.camilo.mercadolibre.search.services.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.exception.ProductsInvalidRequestException;
import org.mercadolibre.camilo.search.exception.ProductsNotFoundException;
import org.mercadolibre.camilo.search.exception.ProductsUpstreamFailureException;
import org.mercadolibre.camilo.search.service.facade.products.ProductsFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.products.model.ProductResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

class ProductsFacadeImplTest {

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
        domains.setProductsBaseUrl(baseUrl);
        domains.setCategoriesBaseUrl(baseUrl);
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
        if (server != null) server.disposeNow();
    }

    @Test
    void getById_success_returnsProduct() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/products/ABC", (req, resp) -> resp
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .sendString(Mono.just("{\"id\":\"ABC\",\"title\":\"Phone\",\"price\":199.99,\"currency\":\"USD\"}"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 0);
        ProductsFacadeImpl facade = new ProductsFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.getById("ABC"))
                .expectNextMatches(p -> p.getId().equals("ABC") && p.getTitle().equals("Phone") && p.getPrice().compareTo(new BigDecimal("199.99")) == 0)
                .verifyComplete();
    }

    @Test
    void getById_notFound_404() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/products/XYZ", (req, resp) -> resp
                        .status(404)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Not Found"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 1);
        ProductsFacadeImpl facade = new ProductsFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.getById("XYZ"))
                .expectError(ProductsNotFoundException.class)
                .verify();
    }

    @Test
    void getById_invalid_400() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/products/BAD", (req, resp) -> resp
                        .status(400)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Bad Request"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 1);
        ProductsFacadeImpl facade = new ProductsFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.getById("BAD"))
                .expectError(ProductsInvalidRequestException.class)
                .verify();
    }

    @Test
    void getById_invalid_422() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/products/UNPROCESSABLE", (req, resp) -> resp
                        .status(422)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Unprocessable"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 1);
        ProductsFacadeImpl facade = new ProductsFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.getById("UNPROCESSABLE"))
                .expectError(ProductsInvalidRequestException.class)
                .verify();
    }

    @Test
    void getById_upstreamFailure_retriesThenFails() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/products/ERR", (req, resp) -> {
                    counter.incrementAndGet();
                    return resp.status(500)
                            .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                            .sendString(Mono.just("Internal Error"));
                }))
                .bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 2;
        EnvironmentConfig env = buildEnv(base, maxRetries);
        ProductsFacadeImpl facade = new ProductsFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.getById("ERR"))
                .expectError(ProductsUpstreamFailureException.class)
                .verify();

        int attempts = counter.get();
        if (attempts != (1 + maxRetries)) {
            throw new AssertionError("Expected " + (1 + maxRetries) + " attempts, got " + attempts);
        }
    }

    @Test
    void getById_upstreamFailure_thenSuccessOnRetry() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/products/RETRY", (req, resp) -> {
                    int attempt = counter.incrementAndGet();
                    if (attempt == 1) {
                        return resp.status(500)
                                .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                .sendString(Mono.just("Internal Error"));
                    }
                    return resp.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .sendString(Mono.just("{\"id\":\"RETRY\",\"title\":\"Recovered\",\"price\":10.50,\"currency\":\"USD\"}"));
                }))
                .bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 3;
        EnvironmentConfig env = buildEnv(base, maxRetries);
        ProductsFacadeImpl facade = new ProductsFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.getById("RETRY"))
                .expectNextMatches(p -> p.getId().equals("RETRY") && p.getTitle().equals("Recovered"))
                .verifyComplete();

        int attempts = counter.get();
        if (attempts != 2) {
            throw new AssertionError("Expected 2 attempts (1 failure + 1 success), got " + attempts);
        }
    }
}

