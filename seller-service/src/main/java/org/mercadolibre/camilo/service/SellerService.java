package org.mercadolibre.camilo.service;

import org.mercadolibre.camilo.dto.SellerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio de negocio para operaciones sobre vendedores.
 */
public interface SellerService {

    /**
     * Obtiene un vendedor por su identificador.
     *
     * @param id identificador del vendedor (no nulo/blank)
     * @return vendedor envuelto en {@link Mono}
     */
    Mono<SellerResponse> get(String id);

    /**
     * Lista todos los vendedores disponibles.
     *
     * @return flujo de vendedores en {@link Flux}
     */
    Flux<SellerResponse> findAll();
}