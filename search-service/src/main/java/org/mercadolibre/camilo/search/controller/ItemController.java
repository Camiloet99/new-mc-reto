package org.mercadolibre.camilo.search.controller;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.search.dto.ItemBasicResponse;
import org.mercadolibre.camilo.search.dto.enriched.ItemEnrichedResponse;
import org.mercadolibre.camilo.search.service.impl.ItemServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
public class ItemController {

    private final ItemServiceImpl service;

    /**
     * Devuelve el detalle básico del producto.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ItemBasicResponse>> getBasic(@PathVariable String id) {
        return service.basic(id)
                .map(body -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body));
    }

    /**
     * Devuelve el detalle enriquecido del producto.
     */
    @GetMapping("/{id}/enriched")
    public Mono<ResponseEntity<ItemEnrichedResponse>> getEnriched(@PathVariable String id) {
        return service.enriched(id)
                .map(body -> ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body));
    }
}
