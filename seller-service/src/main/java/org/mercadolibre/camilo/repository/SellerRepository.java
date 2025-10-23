package org.mercadolibre.camilo.repository;

import org.mercadolibre.camilo.model.Seller;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstracción del acceso a datos de vendedores.
 * <p>
 * Este contrato permite desacoplar la capa de negocio de la fuente de datos
 * subyacente (archivo JSON, base de datos relacional/noSQL, etc.). Las
 * implementaciones deben ser thread-safe o, al menos, no mantener estado
 * mutable compartido.
 */
public interface SellerRepository {

    /**
     * Recupera un vendedor por su identificador.
     *
     * @param id identificador único del vendedor (no debe ser {@code null} ni vacío)
     * @return un {@link Optional} que contiene el {@link Seller} si existe; vacío en caso contrario
     * @implNote Las implementaciones no deben lanzar excepciones por "no encontrado".
     * El caso de ausencia debe representarse con {@code Optional.empty()}.
     */
    Optional<Seller> findById(String id);

    /**
     * Devuelve la colección completa de vendedores.
     *
     * @return una {@link Collection} inmutable (o de solo lectura) con todos los vendedores conocidos;
     * nunca {@code null}. Si no hay datos, debe retornarse una colección vacía.
     * @implNote Aunque se permite retornar una vista directa sobre la estructura interna,
     * no debe ser modificable por el consumidor.
     */
    Collection<Seller> findAll();
}