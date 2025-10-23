package com.mercadolibre.camilo.repository.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.camilo.model.Seller;
import com.mercadolibre.camilo.repository.SellerRepository;
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
public class SellerRepositoryImpl implements SellerRepository {

    /**
     * Índice inmutable de vendedores por ID.
     */
    private final Map<String, Seller> byId;

    /**
     * Crea el repositorio leyendo el JSON configurado y construyendo el índice en memoria.
     *
     * @param data   recurso que apunta al JSON de datos (config: {@code app.data-path})
     * @param mapper {@link ObjectMapper} para deserialización
     * @throws IllegalStateException si ocurre cualquier error al leer o parsear el archivo
     */
    public SellerRepositoryImpl(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        try (InputStream in = data.getInputStream()) {
            List<Seller> list = mapper.readValue(in, new TypeReference<>() {
            });
            Map<String, Seller> map = new HashMap<>();
            for (Seller s : list) map.put(s.getId(), s);
            this.byId = Collections.unmodifiableMap(map);
            log.info("Loaded {} sellers", map.size());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load sellers data", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Seller> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Seller> findAll() {
        return byId.values();
    }
}
