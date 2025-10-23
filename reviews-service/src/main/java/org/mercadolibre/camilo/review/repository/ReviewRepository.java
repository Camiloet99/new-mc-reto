package org.mercadolibre.camilo.review.repository;


import org.mercadolibre.camilo.review.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {

    /**
     * Obtiene una reseña por su identificador.
     *
     * @param id id de la reseña
     * @return reseña si existe; vacío si no
     */
    Optional<Review> findById(String id);

    /**
     * Lista reseñas de un producto, ordenadas por fecha de creación descendente.
     *
     * @param productId id del producto
     * @return lista inmutable (vacía si no hay reseñas)
     */
    List<Review> findByProductId(String productId);
}