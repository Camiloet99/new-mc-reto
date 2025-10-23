package org.mercadolibre.camilo.category.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mercadolibre.camilo.category.exception.CategoriesDataLoadException;
import org.mercadolibre.camilo.category.model.Category;
import org.mercadolibre.camilo.category.repository.CategoryRepository;
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
public class CategoryRepositoryImpl implements CategoryRepository {

    private final Map<String, Category> byId;
    private final Map<String, List<Category>> childrenIndex;

    /**
     * Crea el repositorio leyendo el JSON configurado y construyendo índices inmutables.
     *
     * @param data   recurso JSON (prop: {@code app.data-path})
     * @param mapper ObjectMapper para deserializar
     * @throws CategoriesDataLoadException cuando hay errores de I/O o parseo
     */
    public CategoryRepositoryImpl(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        Objects.requireNonNull(data, "Resource 'data' must not be null");
        Objects.requireNonNull(mapper, "ObjectMapper must not be null");

        try (InputStream in = data.getInputStream()) {
            List<Category> list = mapper.readValue(in, new TypeReference<>() {
            });
            Map<String, Category> map = new HashMap<>(Math.max(16, list.size() * 2));
            Map<String, List<Category>> children = new HashMap<>();

            int invalid = 0;
            int duplicates = 0;

            for (int i = 0; i < list.size(); i++) {
                Category c = list.get(i);
                if (c == null || c.getId() == null || c.getId().isBlank()) {
                    invalid++;
                    log.warn("CategoryRepositoryImpl | skipping invalid record at index={} (missing id)", i);
                    continue;
                }

                Category prev = map.put(c.getId(), c);
                if (prev != null) {
                    duplicates++;
                    log.warn("CategoryRepositoryImpl | duplicate id='{}' at index={} (last-wins)", c.getId(), i);
                }

                if (c.getParentId() != null && !c.getParentId().isBlank()) {
                    children.computeIfAbsent(c.getParentId(), k -> new ArrayList<>()).add(c);
                }
            }

            this.byId = Collections.unmodifiableMap(map);
            this.childrenIndex = unmodifiableChildren(children);

            log.info("Loaded {} categories ({} invalid, {} duplicates resolved last-wins) from {}",
                    map.size(), invalid, duplicates, safeDesc(data));

            if (map.isEmpty()) {
                log.warn("CategoryRepositoryImpl | no categories loaded from {}", safeDesc(data));
            }

        } catch (Exception e) {
            throw new CategoriesDataLoadException("Cannot load categories data from " + safeDesc(data), e);
        }
    }

    private static Map<String, List<Category>> unmodifiableChildren(Map<String, List<Category>> src) {
        Map<String, List<Category>> out = new HashMap<>(src.size());
        for (Map.Entry<String, List<Category>> e : src.entrySet()) {
            out.put(e.getKey(), Collections.unmodifiableList(new ArrayList<>(e.getValue())));
        }
        return Collections.unmodifiableMap(out);
    }

    private String safeDesc(Resource r) {
        try {
            return r.getDescription();
        } catch (Exception ignored) {
            return String.valueOf(r);
        }
    }

    @Override
    public Optional<Category> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Category> childrenOf(String id) {
        return childrenIndex.getOrDefault(id, List.of());
    }

    @Override
    public Map<String, Category> getById() {
        return byId;
    }
}