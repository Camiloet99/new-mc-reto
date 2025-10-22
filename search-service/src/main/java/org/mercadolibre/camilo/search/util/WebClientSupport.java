package org.mercadolibre.camilo.search.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@UtilityClass
public class WebClientSupport {

    public static <T> Mono<T> mapResponse(
            ClientResponse response,
            Class<T> bodyClass,
            Function<Context, RuntimeException> notFoundEx,
            Function<Context, RuntimeException> invalidReqEx,
            Function<Context, RuntimeException> upstreamEx) {

        HttpStatusCode status = response.statusCode();
        HttpHeaders headers = response.headers().asHttpHeaders();

        if (status.is2xxSuccessful()) {
            return response.bodyToMono(bodyClass);
        }

        return response.bodyToMono(String.class).defaultIfEmpty("")
                .flatMap(body -> {
                    Context ctx = new Context(status.value(), headers, response.logPrefix(), body);
                    int sc = status.value();
                    if (sc == 404) return Mono.error(notFoundEx.apply(ctx));
                    if (sc == 400 || sc == 422) return Mono.error(invalidReqEx.apply(ctx));
                    return Mono.error(upstreamEx.apply(ctx));
                });
    }

    public record Context(int status, HttpHeaders headers, String uri, String body) {
    }
}