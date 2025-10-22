package com.mercadolibre.camilo.products.controller;

import com.mercadolibre.camilo.products.dto.ProductResponse;
import com.mercadolibre.camilo.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductService service;

    /**
     * Lista productos aplicando filtros opcionales:
     * <ul>
     *   <li>{@code categoryId}: coincidencia exacta</li>
     *   <li>{@code sellerId}: coincidencia exacta</li>
     *   <li>{@code q}: contiene en el título (case-insensitive)</li>
     * </ul>
     *
     * @param categoryId (opcional) id de categoría
     * @param sellerId   (opcional) id de vendedor
     * @param q          (opcional) texto de búsqueda en título
     * @return {@link ResponseEntity} con un {@link Flux} de {@link ProductResponse}
     */
    @GetMapping
    public ResponseEntity<Flux<ProductResponse>> getAll(
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "sellerId", required = false) String sellerId,
            @RequestParam(value = "q", required = false) String q) {

        Flux<ProductResponse> body = service.findAll(categoryId, sellerId, q);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id identificador del producto
     * @return {@link Mono} de {@link ResponseEntity}:
     * <ul>
     *   <li>200 OK con el {@link ProductResponse} si existe</li>
     *   <li>404 Not Found si no existe (mapeado por el handler global)</li>
     * </ul>
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductResponse>> get(@PathVariable String id) {
        return service.get(id)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }
}