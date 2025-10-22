package com.mercadolibre.camilo.category.repository;

import com.mercadolibre.camilo.category.model.Category;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CategoryRepository {

    /**
     * Busca una categoría por su identificador.
     *
     * @param id identificador único
     * @return {@link Optional} con la categoría si existe; vacío en caso contrario
     */
    Optional<Category> findById(String id);

    /**
     * Devuelve los hijos directos de la categoría dada.
     *
     * @param id identificador del padre
     * @return lista inmutable de categorías hijas; vacía si no hay
     */
    List<Category> childrenOf(String id);

    /**
     * Índice inmutable id → categoría (solo lectura).
     *
     * @return mapa inmutable con todas las categorías
     */
    Map<String, Category> getById();
}
