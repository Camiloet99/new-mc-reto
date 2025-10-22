package com.mercadolibre.camilo.category.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.camilo.category.model.Category;
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
public class CategoryRepositoryImpl {

    private final Map<String, Category> byId;
    private final Map<String, List<Category>> childrenIndex;

    public CategoryRepositoryImpl(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        try (InputStream in = data.getInputStream()) {
            List<Category> list = mapper.readValue(in, new TypeReference<>() {
            });
            Map<String, Category> map = new HashMap<>();
            Map<String, List<Category>> children = new HashMap<>();
            for (Category c : list) {
                map.put(c.getId(), c);
                if (c.getParentId() != null) {
                    children.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
                }
            }
            this.byId = Collections.unmodifiableMap(map);
            this.childrenIndex = Collections.unmodifiableMap(children);
            log.info("Loaded {} categories", map.size());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load categories data", e);
        }
    }

    public Optional<Category> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Category> childrenOf(String id) {
        return childrenIndex.getOrDefault(id, List.of());
    }
}
