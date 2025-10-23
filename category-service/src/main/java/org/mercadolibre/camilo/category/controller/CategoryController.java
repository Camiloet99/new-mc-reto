package org.mercadolibre.camilo.category.controller;

import org.mercadolibre.camilo.category.dto.BreadcrumbNode;
import org.mercadolibre.camilo.category.dto.CategoryResponse;
import org.mercadolibre.camilo.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/categories", produces = MediaType.APPLICATION_JSON_VALUE)
public class CategoryController {

    private final CategoryService service;

    /**
     * Obtiene todas las categorías o, si se especifica {@code parentId}, únicamente
     * los hijos directos de esa categoría.
     *
     * @param parentId (opcional) id de la categoría padre para filtrar sus hijos
     * @return {@link ResponseEntity} con un {@link Flux} de {@link CategoryResponse}
     * (200 OK siempre; si no hay resultados, el flujo vendrá vacío)
     */
    @GetMapping
    public ResponseEntity<Flux<CategoryResponse>> getAll(
            @RequestParam(value = "parentId", required = false) String parentId) {

        Flux<CategoryResponse> body = service.findAll(parentId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    /**
     * Obtiene una categoría por id, incluyendo los campos derivados:
     * {@code pathFromRoot} y {@code childrenCount}.
     *
     * @param id identificador de la categoría
     * @return {@link Mono} con {@link ResponseEntity}:
     * <ul>
     *   <li>200 OK y el {@link CategoryResponse} si existe</li>
     *   <li>404 Not Found si no existe (mapeado por el handler global)</li>
     * </ul>
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<CategoryResponse>> get(@PathVariable String id) {
        return service.getWithDerived(id)
                .map(body -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body));
    }

    /**
     * Devuelve el breadcrumb ({@code pathFromRoot}) de una categoría,
     * incluyendo el nodo raíz y la propia categoría.
     *
     * @param id identificador de la categoría
     * @return {@link Mono} con {@link ResponseEntity}:
     * <ul>
     *   <li>200 OK y la lista de {@link BreadcrumbNode}</li>
     *   <li>404 Not Found si la categoría no existe</li>
     * </ul>
     */
    @GetMapping("/{id}/breadcrumb")
    public Mono<ResponseEntity<List<BreadcrumbNode>>> breadcrumb(@PathVariable String id) {
        return service.breadcrumb(id)
                .map(path -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(path));
    }
}