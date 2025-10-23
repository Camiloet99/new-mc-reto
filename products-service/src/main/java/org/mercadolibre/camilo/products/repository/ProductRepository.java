package org.mercadolibre.camilo.products.repository;


import org.mercadolibre.camilo.products.model.Product;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstracción del acceso a datos de productos.
 * <p>
 * Permite desacoplar la capa de negocio de la fuente de datos subyacente
 * (archivo JSON, base de datos, etc.). Las implementaciones deben exponer
 * vistas inmutables o de solo lectura.
 */
public interface ProductRepository {

    /**
     * Busca un producto por su identificador.
     *
     * @param id identificador del producto (no debe ser {@code null} ni vacío)
     * @return {@link Optional} con el producto si existe; vacío en caso contrario
     */
    Optional<Product> findById(String id);

    /**
     * Devuelve todos los productos disponibles.
     *
     * @return colección de solo lectura con los productos; nunca {@code null}
     */
    Collection<Product> findAll();
}