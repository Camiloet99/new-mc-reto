package com.mercadolibre.camilo.products.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.camilo.products.model.Product;
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
public class ProductRepository {

    private final Map<String, Product> byId;

    public ProductRepository(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        try (InputStream in = data.getInputStream()) {
            List<Product> list = mapper.readValue(in, new TypeReference<List<Product>>() {});
            Map<String, Product> map = new HashMap<>();
            for (Product p : list) map.put(p.getId(), p);
            this.byId = Collections.unmodifiableMap(map);
            log.info("Loaded {} products", map.size());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load products data", e);
        }
    }

    public Optional<Product> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Collection<Product> findAll() {
        return byId.values();
    }
}
