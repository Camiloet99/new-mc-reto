package com.mercadolibre.camilo.controller;

import com.mercadolibre.camilo.dto.SellerResponse;
import com.mercadolibre.camilo.service.SellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/sellers", produces = MediaType.APPLICATION_JSON_VALUE)
public class SellerController {

    private final SellerService service;

    /**
     * Devuelve todos los vendedores disponibles.
     *
     * @return {@link ResponseEntity} con un {@link Flux} de {@link SellerResponse}
     * (200 OK; si no hay datos, el flujo vendrá vacío)
     */
    @GetMapping
    public ResponseEntity<Flux<SellerResponse>> getAll() {
        Flux<SellerResponse> body = service.findAll();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Obtiene un vendedor por su identificador.
     *
     * @param id identificador del vendedor
     * @return {@link Mono} de {@link ResponseEntity}:
     * <ul>
     *   <li>200 OK con el {@link SellerResponse} si existe</li>
     *   <li>404 Not Found si no existe (mapeado por el handler global)</li>
     * </ul>
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<SellerResponse>> get(@PathVariable String id) {
        return service.get(id)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }
}
