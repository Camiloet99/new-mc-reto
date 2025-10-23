package org.camilo.mercadolibre.search.services.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.exception.ReviewsInvalidRequestException;
import org.mercadolibre.camilo.search.exception.ReviewsNotFoundException;
import org.mercadolibre.camilo.search.exception.ReviewsUpstreamFailureException;
import org.mercadolibre.camilo.search.service.facade.reviews.ReviewsFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.reviews.model.ReviewResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ReviewsFacadeImplTest {

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
        domains.setReviewsBaseUrl(baseUrl);
        domains.setProductsBaseUrl(baseUrl);
        domains.setCategoriesBaseUrl(baseUrl);
        domains.setSellersBaseUrl(baseUrl);
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
    void list_success_returnsList() {
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> resp
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .sendString(Mono.just("[" +
                        "{\"id\":\"R1\",\"productId\":\"P1\",\"rating\":5,\"title\":\"Great\",\"text\":\"Excellent product\",\"createdAt\":\"2024-01-01\",\"author\":\"User1\"}," +
                        "{\"id\":\"R2\",\"productId\":\"P1\",\"rating\":3,\"title\":\"Ok\",\"text\":\"Average\",\"createdAt\":\"2024-01-02\",\"author\":\"User2\"}" +
                        "]"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, 0));

        StepVerifier.create(facade.list("P1"))
                .expectNextMatches(list -> list.size() == 2 && list.get(0).getId().equals("R1") && list.get(1).getRating() == 3)
                .verifyComplete();
    }

    @Test
    void list_trimsInput_success() {
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> resp
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .sendString(Mono.just("[{\"id\":\"R1\",\"productId\":\"P2\",\"rating\":4,\"title\":\"Good\",\"text\":\"Nice\",\"createdAt\":\"2024-01-01\",\"author\":\"User1\"}]"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, 0));

        StepVerifier.create(facade.list("  P2  "))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getProductId().equals("P2"))
                .verifyComplete();
    }

    @Test
    void list_emptyArray_returnsEmptyList() {
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> resp
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .sendString(Mono.just("[]"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, 0));

        StepVerifier.create(facade.list("P3"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void list_nullBody_returnsEmptyList() {
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> resp
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .sendString(Mono.just("null"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, 0));

        StepVerifier.create(facade.list("P4"))
                .expectNextMatches(List::isEmpty)
                .verifyComplete();
    }

    @Test
    void list_notFound_404() {
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> resp
                .status(404)
                .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .sendString(Mono.just("Not Found"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, 1));

        StepVerifier.create(facade.list("MISSING"))
                .expectError(ReviewsNotFoundException.class)
                .verify();
    }

    @Test
    void list_invalidRequest_400() {
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> resp
                .status(400)
                .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .sendString(Mono.just("Bad Request"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, 1));

        StepVerifier.create(facade.list("BAD"))
                .expectError(ReviewsInvalidRequestException.class)
                .verify();
    }

    @Test
    void list_invalidRequest_422() {
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> resp
                .status(422)
                .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                .sendString(Mono.just("Unprocessable"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, 1));

        StepVerifier.create(facade.list("UNPROC"))
                .expectError(ReviewsInvalidRequestException.class)
                .verify();
    }

    @Test
    void list_upstreamFailure_retriesThenFails() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> {
            counter.incrementAndGet();
            return resp.status(500)
                    .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                    .sendString(Mono.just("Internal Error"));
        })).bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 2;
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, maxRetries));

        StepVerifier.create(facade.list("ERR"))
                .expectError(ReviewsUpstreamFailureException.class)
                .verify();

        int attempts = counter.get();
        if (attempts != (1 + maxRetries)) {
            throw new AssertionError("Expected " + (1 + maxRetries) + " attempts, got " + attempts);
        }
    }

    @Test
    void list_upstreamFailure_thenSuccessOnRetry() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create().port(0).route(r -> r.get("/reviews", (req, resp) -> {
            int attempt = counter.incrementAndGet();
            if (attempt == 1) {
                return resp.status(500)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Internal Error"));
            }
            return resp.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .sendString(Mono.just("[{\"id\":\"R9\",\"productId\":\"PX\",\"rating\":2,\"title\":\"Low\",\"text\":\"Bad\",\"createdAt\":\"2024-01-03\",\"author\":\"UserX\"}]"));
        })).bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 3;
        ReviewsFacadeImpl facade = new ReviewsFacadeImpl(WebClient.builder().build(), buildEnv(base, maxRetries));

        StepVerifier.create(facade.list("PX"))
                .expectNextMatches(list -> list.size() == 1 && list.get(0).getId().equals("R9"))
                .verifyComplete();

        int attempts = counter.get();
        if (attempts != 2) {
            throw new AssertionError("Expected 2 attempts (1 failure + 1 success), got " + attempts);
        }
    }
}

