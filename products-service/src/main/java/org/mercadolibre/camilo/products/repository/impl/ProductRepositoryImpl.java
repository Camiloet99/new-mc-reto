package org.mercadolibre.camilo.products.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mercadolibre.camilo.products.exception.ProductsDataLoadException;
import org.mercadolibre.camilo.products.model.Product;
import org.mercadolibre.camilo.products.repository.ProductRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Getter
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    /**
     * Índice inmutable de productos por ID.
     */
    private final Map<String, Product> byId;

    /**
     * Crea el repositorio leyendo el JSON configurado y construyendo el índice en memoria.
     *
     * @param data   recurso que apunta al JSON de datos (config: {@code app.data-path})
     * @param mapper {@link ObjectMapper} para deserialización
     * @throws ProductsDataLoadException si ocurre cualquier error al leer o parsear el archivo
     */
    public ProductRepositoryImpl(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        Objects.requireNonNull(data, "Resource 'data' must not be null");
        Objects.requireNonNull(mapper, "ObjectMapper must not be null");

        try (InputStream in = data.getInputStream()) {
            List<Product> list = mapper.readValue(in, new TypeReference<>() {
            });
            Map<String, Product> map = new HashMap<>(Math.max(16, list.size() * 2));

            int duplicates = 0;
            int invalid = 0;

            for (int i = 0; i < list.size(); i++) {
                Product p = list.get(i);
                if (p == null || p.getId() == null || p.getId().isBlank()) {
                    invalid++;
                    log.warn("ProductRepositoryImpl | skipping invalid record at index={} (missing id)", i);
                    continue;
                }

                Product prev = map.put(p.getId(), p);
                if (prev != null) {
                    duplicates++;
                    log.warn("ProductRepositoryImpl | duplicate id='{}' at index={} (last-wins)", p.getId(), i);
                }
            }

            this.byId = Collections.unmodifiableMap(map);
            log.info("Loaded {} products ({} invalid, {} duplicates resolved last-wins)",
                    map.size(), invalid, duplicates);

            if (map.isEmpty()) {
                log.warn("ProductRepositoryImpl | no products loaded from {}", safeDesc(data));
            }

        } catch (Exception e) {
            throw new ProductsDataLoadException("Cannot load products data from " + safeDesc(data), e);
        }
    }

    private String safeDesc(Resource r) {
        try {
            return r.getDescription();
        } catch (Exception ignored) {
            return r.toString();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Product> findAll() {
        return byId.values();
    }
}
