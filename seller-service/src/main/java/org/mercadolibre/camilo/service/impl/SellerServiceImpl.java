package org.mercadolibre.camilo.service.impl;

import org.mercadolibre.camilo.dto.SellerResponse;
import org.mercadolibre.camilo.exceptions.InvalidRequestException;
import org.mercadolibre.camilo.exceptions.SellerNotFoundException;
import org.mercadolibre.camilo.repository.SellerRepository;
import org.mercadolibre.camilo.service.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación reactiva de {@link SellerService}.
 * <p>
 * Aplica validaciones de entrada, logs de diagnóstico y mapeo de excepciones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {

    private final SellerRepository repo;

    /**
     * {@inheritDoc}
     * <p>
     * Errores:
     * <ul>
     *   <li>{@link InvalidRequestException} si {@code id} es nulo o en blanco (HTTP 400).</li>
     *   <li>{@link SellerNotFoundException} si no existe el vendedor (HTTP 404).</li>
     * </ul>
     */
    @Override
    public Mono<SellerResponse> get(String id) {
        if (id == null || id.isBlank()) {
            log.warn("SellerService.get | invalid id (blank)");
            return Mono.error(new InvalidRequestException("Seller id must not be blank"));
        }

        log.info("SellerService.get | fetching seller | id={}", id);
        return Mono.defer(() -> Mono.justOrEmpty(repo.findById(id)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("SellerService.get | seller not found | id={}", id);
                    return Mono.error(new SellerNotFoundException(id));
                }))
                .map(s -> {
                    log.debug("SellerService.get | seller loaded | id={} nickname={} reputation={}",
                            s.getId(), s.getNickname(), s.getReputation());
                    return SellerResponse.from(s);
                })
                .doOnError(ex -> log.error("SellerService.get | error | id={} | type={} | msg={}",
                        id, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<SellerResponse> findAll() {
        log.info("SellerService.findAll | fetching all sellers");
        return Flux.fromIterable(repo.findAll())
                .map(SellerResponse::from)
                .doOnComplete(() -> log.debug("SellerService.findAll | completed"));
    }
}
