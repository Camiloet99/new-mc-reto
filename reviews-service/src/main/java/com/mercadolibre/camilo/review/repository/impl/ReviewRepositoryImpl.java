package com.mercadolibre.camilo.review.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.camilo.review.exception.ReviewsDataLoadException;
import com.mercadolibre.camilo.review.model.Review;
import com.mercadolibre.camilo.review.repository.ReviewRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Repository
public class ReviewRepositoryImpl implements ReviewRepository {

    private final Map<String, Review> byId;
    private final Map<String, List<Review>> byProduct;

    /**
     * Crea el repositorio leyendo el JSON y construyendo índices inmutables.
     * <ul>
     *   <li>Ignora registros inválidos (sin id o sin productId) y loguea advertencia.</li>
     *   <li>Si hay ids duplicados, prevalece el último (last-wins) con advertencia.</li>
     *   <li>Ordena las reseñas por producto en <b>fecha desc</b> (más reciente primero).</li>
     * </ul>
     *
     * @param data   recurso JSON (prop: {@code app.data-path})
     * @param mapper {@link ObjectMapper} para deserializar
     * @throws ReviewsDataLoadException si hay error de lectura/parseo
     */
    public ReviewRepositoryImpl(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        Objects.requireNonNull(data, "Resource 'data' must not be null");
        Objects.requireNonNull(mapper, "ObjectMapper must not be null");

        try (InputStream in = data.getInputStream()) {
            List<Review> raw = mapper.readValue(in, new TypeReference<>() {
            });
            Map<String, Review> idMap = new HashMap<>(Math.max(16, raw.size() * 2));
            Map<String, List<Review>> prodMap = new HashMap<>();

            int invalid = 0;
            int duplicates = 0;

            for (int i = 0; i < raw.size(); i++) {
                Review r = raw.get(i);

                if (r == null || isBlank(r.getId()) || isBlank(r.getProductId())) {
                    invalid++;
                    log.warn("ReviewRepositoryImpl | skipping invalid record at index={} (missing id/productId)", i);
                    continue;
                }

                Review prev = idMap.put(r.getId(), r);
                if (prev != null) {
                    duplicates++;
                    log.warn("ReviewRepositoryImpl | duplicate review id='{}' at index={} (last-wins)", r.getId(), i);
                }

                prodMap.computeIfAbsent(r.getProductId(), k -> new ArrayList<>()).add(r);
            }

            prodMap.replaceAll((k, v) -> v.stream()
                    .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                    .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)));

            this.byId = Collections.unmodifiableMap(idMap);
            this.byProduct = Collections.unmodifiableMap(prodMap);

            log.info("Loaded {} reviews ({} invalid, {} duplicates resolved last-wins) from {}",
                    idMap.size(), invalid, duplicates, safeDesc(data));

            if (idMap.isEmpty()) {
                log.warn("ReviewRepositoryImpl | no reviews loaded from {}", safeDesc(data));
            }

        } catch (Exception e) {
            throw new ReviewsDataLoadException("Cannot load reviews data from " + safeDesc(data), e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String safeDesc(Resource r) {
        try {
            return r.getDescription();
        } catch (Exception ignored) {
            return String.valueOf(r);
        }
    }

    @Override
    public Optional<Review> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Review> findByProductId(String productId) {
        return byProduct.getOrDefault(productId, List.of());
    }
}