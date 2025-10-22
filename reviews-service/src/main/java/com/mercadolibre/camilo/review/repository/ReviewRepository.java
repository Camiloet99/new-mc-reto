package com.mercadolibre.camilo.review.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.camilo.review.model.Review;
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
public class ReviewRepository {

    private final Map<String, Review> byId;
    private final Map<String, List<Review>> byProduct;

    public ReviewRepository(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        try (InputStream in = data.getInputStream()) {
            List<Review> list = mapper.readValue(in, new TypeReference<>() {
            });
            Map<String, Review> idMap = new HashMap<>();
            Map<String, List<Review>> prodMap = new HashMap<>();
            for (Review r : list) {
                idMap.put(r.getId(), r);
                prodMap.computeIfAbsent(r.getProductId(), k -> new ArrayList<>()).add(r);
            }
            prodMap.replaceAll((k, v) -> v.stream()
                    .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                    .collect(Collectors.toList()));

            this.byId = Collections.unmodifiableMap(idMap);
            this.byProduct = Collections.unmodifiableMap(prodMap);
            log.info("Loaded {} reviews", idMap.size());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load reviews data", e);
        }
    }

    public List<Review> findByProductId(String productId) {
        return byProduct.getOrDefault(productId, List.of());
    }
}