package org.camilo.mercadolibre.search.services.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.exception.QaInvalidRequestException;
import org.mercadolibre.camilo.search.exception.QaNotFoundException;
import org.mercadolibre.camilo.search.exception.QaUpstreamFailureException;
import org.mercadolibre.camilo.search.service.facade.qa.QaFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.qa.model.QaResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class QaFacadeImplTest {

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
        domains.setQaBaseUrl(baseUrl);
        domains.setCategoriesBaseUrl(baseUrl);
        domains.setProductsBaseUrl(baseUrl);
        domains.setSellersBaseUrl(baseUrl);
        domains.setReviewsBaseUrl(baseUrl);
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
    void listByProduct_success_returnsList() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/qa", (req, resp) -> resp
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .sendString(Mono.just("[{\"id\":\"Q1\",\"productId\":\"P1\",\"author\":\"UserA\",\"text\":\"Question?\",\"createdAt\":\"2024-01-01\",\"answers\":[{\"id\":\"A1\",\"questionId\":\"Q1\",\"author\":\"Seller\",\"text\":\"Answer\",\"createdAt\":\"2024-01-02\"}]}]"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 0);
        QaFacadeImpl facade = new QaFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.listByProduct("P1"))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getId().equals("Q1") && list.get(0).getAnswers().size() == 1)
                .verifyComplete();
    }

    @Test
    void listByProduct_notFound_404() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/qa", (req, resp) -> resp
                        .status(404)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Not Found"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 1);
        QaFacadeImpl facade = new QaFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.listByProduct("MISSING"))
                .expectError(QaNotFoundException.class)
                .verify();
    }

    @Test
    void listByProduct_invalidRequest_400() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/qa", (req, resp) -> resp
                        .status(400)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Bad Request"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 1);
        QaFacadeImpl facade = new QaFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.listByProduct("BAD"))
                .expectError(QaInvalidRequestException.class)
                .verify();
    }

    @Test
    void listByProduct_invalidRequest_422() {
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/qa", (req, resp) -> resp
                        .status(422)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Unprocessable"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        EnvironmentConfig env = buildEnv(base, 1);
        QaFacadeImpl facade = new QaFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.listByProduct("UNPROC"))
                .expectError(QaInvalidRequestException.class)
                .verify();
    }

    @Test
    void listByProduct_upstreamFailure_retriesThenFails() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/qa", (req, resp) -> {
                    counter.incrementAndGet();
                    return resp.status(500)
                            .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                            .sendString(Mono.just("Internal Error"));
                }))
                .bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 2;
        EnvironmentConfig env = buildEnv(base, maxRetries);
        QaFacadeImpl facade = new QaFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.listByProduct("ERR"))
                .expectError(QaUpstreamFailureException.class)
                .verify();

        int attempts = counter.get();
        if (attempts != (1 + maxRetries)) {
            throw new AssertionError("Expected " + (1 + maxRetries) + " attempts, got " + attempts);
        }
    }

    @Test
    void listByProduct_upstreamFailure_thenSuccessOnRetry() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create()
                .port(0)
                .route(r -> r.get("/qa", (req, resp) -> {
                    int attempt = counter.incrementAndGet();
                    if (attempt == 1) {
                        return resp.status(500)
                                .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                                .sendString(Mono.just("Internal Error"));
                    }
                    return resp.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                            .sendString(Mono.just("[{\"id\":\"Q2\",\"productId\":\"P2\",\"author\":\"UserB\",\"text\":\"Another?\",\"createdAt\":\"2024-02-01\",\"answers\":[]}]"));
                }))
                .bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 3;
        EnvironmentConfig env = buildEnv(base, maxRetries);
        QaFacadeImpl facade = new QaFacadeImpl(WebClient.builder().build(), env);

        StepVerifier.create(facade.listByProduct("P2"))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getId().equals("Q2"))
                .verifyComplete();

        int attempts = counter.get();
        if (attempts != 2) {
            throw new AssertionError("Expected 2 attempts (1 failure + 1 success), got " + attempts);
        }
    }
}

