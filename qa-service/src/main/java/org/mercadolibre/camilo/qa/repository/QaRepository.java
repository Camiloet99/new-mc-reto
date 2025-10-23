package org.mercadolibre.camilo.qa.repository;

import org.mercadolibre.camilo.qa.model.Question;

import java.util.List;
import java.util.Optional;

/**
 * Abstracción de acceso a datos de Q&A.
 */
public interface QaRepository {

    /**
     * Busca una pregunta por su identificador.
     * @param id identificador de la pregunta
     * @return pregunta si existe; vacío en caso contrario
     */
    Optional<Question> findById(String id);

    /**
     * Lista las preguntas de un producto, ordenadas por fecha de creación descendente.
     * @param productId identificador del producto
     * @return lista inmutable (vacía si no hay preguntas)
     */
    List<Question> findByProductId(String productId);
}
