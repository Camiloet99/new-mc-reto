package org.camilo.mercadolibre.search.services.facade;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mercadolibre.camilo.search.config.EnvironmentConfig;
import org.mercadolibre.camilo.search.exception.SellersInvalidRequestException;
import org.mercadolibre.camilo.search.exception.SellersNotFoundException;
import org.mercadolibre.camilo.search.exception.SellersUpstreamFailureException;
import org.mercadolibre.camilo.search.service.facade.seller.SellersFacadeImpl;
import org.mercadolibre.camilo.search.service.facade.seller.model.SellerResponse;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

class SellersFacadeImplTest {

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
        domains.setSellersBaseUrl(baseUrl);
        domains.setProductsBaseUrl(baseUrl);
        domains.setCategoriesBaseUrl(baseUrl);
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
    void getById_success_returnsSeller() {
        server = HttpServer.create().port(0).route(r -> r.get("/sellers/ABC", (req, resp) -> resp
                        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .sendString(Mono.just("{\"id\":\"ABC\",\"nickname\":\"seller_abc\",\"reputation\":0.87,\"metrics\":{\"cancellations\":0.02,\"delays\":0.05}}"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        SellersFacadeImpl facade = new SellersFacadeImpl(WebClient.builder().build(), buildEnv(base, 0));

        StepVerifier.create(facade.getById("ABC"))
                .expectNextMatches(s -> s.getId().equals("ABC") && s.getNickname().equals("seller_abc") && s.getMetrics().getCancellations() == 0.02)
                .verifyComplete();
    }

    @Test
    void getById_notFound_404() {
        server = HttpServer.create().port(0).route(r -> r.get("/sellers/XYZ", (req, resp) -> resp
                        .status(404)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Not Found"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        SellersFacadeImpl facade = new SellersFacadeImpl(WebClient.builder().build(), buildEnv(base, 1));

        StepVerifier.create(facade.getById("XYZ"))
                .expectError(SellersNotFoundException.class)
                .verify();
    }

    @Test
    void getById_invalidRequest_400() {
        server = HttpServer.create().port(0).route(r -> r.get("/sellers/BAD", (req, resp) -> resp
                        .status(400)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Bad Request"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        SellersFacadeImpl facade = new SellersFacadeImpl(WebClient.builder().build(), buildEnv(base, 1));

        StepVerifier.create(facade.getById("BAD"))
                .expectError(SellersInvalidRequestException.class)
                .verify();
    }

    @Test
    void getById_invalidRequest_422() {
        server = HttpServer.create().port(0).route(r -> r.get("/sellers/UNPROC", (req, resp) -> resp
                        .status(422)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Unprocessable"))))
                .bindNow();
        String base = "http://localhost:" + server.port();
        SellersFacadeImpl facade = new SellersFacadeImpl(WebClient.builder().build(), buildEnv(base, 1));

        StepVerifier.create(facade.getById("UNPROC"))
                .expectError(SellersInvalidRequestException.class)
                .verify();
    }

    @Test
    void getById_upstreamFailure_retriesThenFails() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create().port(0).route(r -> r.get("/sellers/ERR", (req, resp) -> {
            counter.incrementAndGet();
            return resp.status(500)
                    .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                    .sendString(Mono.just("Internal Error"));
        })).bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 2;
        SellersFacadeImpl facade = new SellersFacadeImpl(WebClient.builder().build(), buildEnv(base, maxRetries));

        StepVerifier.create(facade.getById("ERR"))
                .expectError(SellersUpstreamFailureException.class)
                .verify();

        int attempts = counter.get();
        if (attempts != (1 + maxRetries)) {
            throw new AssertionError("Expected " + (1 + maxRetries) + " attempts, got " + attempts);
        }
    }

    @Test
    void getById_upstreamFailure_thenSuccessOnRetry() {
        AtomicInteger counter = new AtomicInteger();
        server = HttpServer.create().port(0).route(r -> r.get("/sellers/RETRY", (req, resp) -> {
            int attempt = counter.incrementAndGet();
            if (attempt == 1) {
                return resp.status(500)
                        .header("Content-Type", MediaType.TEXT_PLAIN_VALUE)
                        .sendString(Mono.just("Internal Error"));
            }
            return resp.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .sendString(Mono.just("{\"id\":\"RETRY\",\"nickname\":\"seller_retry\",\"reputation\":0.45,\"metrics\":{\"cancellations\":0.01,\"delays\":0.02}}"));
        })).bindNow();
        String base = "http://localhost:" + server.port();
        int maxRetries = 3;
        SellersFacadeImpl facade = new SellersFacadeImpl(WebClient.builder().build(), buildEnv(base, maxRetries));

        StepVerifier.create(facade.getById("RETRY"))
                .expectNextMatches(s -> s.getId().equals("RETRY") && s.getMetrics().getDelays() == 0.02)
                .verifyComplete();

        int attempts = counter.get();
        if (attempts != 2) {
            throw new AssertionError("Expected 2 attempts (1 failure + 1 success), got " + attempts);
        }
    }
}

