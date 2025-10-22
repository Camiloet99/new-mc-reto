package com.mercadolibre.camilo.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.camilo.model.Seller;
import com.mercadolibre.camilo.repository.impl.SellerRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para {@link SellerRepositoryImpl}.
 * Combina carga desde classpath (archivo real) con recursos en memoria para casos de borde.
 */
class SellerRepositoryImplTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static Resource classpath(String path) {
        return new ClassPathResource(path);
    }

    private static Resource bytes(String json) {
        return new ByteArrayResource(json.getBytes()) {
            @Override public String getDescription() { return "in-memory-json"; }
        };
    }

    @Nested
    @DisplayName("Carga desde classpath (archivo real)")
    class LoadFromClasspath {

        @Test
        @DisplayName("debe cargar todos los sellers y permitir consultas básicas")
        void load_ok_fromClasspath_andQuery() throws Exception {
            Resource res = classpath("testdata/sellers.json");
            List<Seller> expected = mapper.readValue(res.getInputStream(), new TypeReference<List<Seller>>() {});

            SellerRepositoryImpl repo = new SellerRepositoryImpl(res, mapper);

            Collection<Seller> all = repo.findAll();
            assertThat(all).hasSize(expected.size());

            Seller first = expected.get(0);
            Optional<Seller> loaded = repo.findById(first.getId());
            assertThat(loaded).isPresent();
            assertThat(loaded.get().getNickname()).isEqualTo(first.getNickname());
            assertThat(loaded.get().getReputation()).isEqualTo(first.getReputation());
        }
    }

    @Nested
    @DisplayName("Casos de borde e inmutabilidad")
    class EdgeCases {

        @Test
        @DisplayName("IDs duplicados → el último prevalece")
        void duplicates_lastWins() {
            String json = """
        [
          {"id":"S-1","nickname":"TechStore","reputation":4.7,"metrics":{"cancellations":0.03,"delays":0.10}},
          {"id":"S-1","nickname":"TechStore-UPDATED","reputation":4.9,"metrics":{"cancellations":0.02,"delays":0.05}}
        ]
        """;
            Resource res = bytes(json);

            SellerRepositoryImpl repo = new SellerRepositoryImpl(res, mapper);

            Optional<Seller> s1 = repo.findById("S-1");
            assertThat(s1).isPresent();
            assertThat(s1.get().getNickname()).isEqualTo("TechStore-UPDATED");
            assertThat(s1.get().getReputation()).isEqualTo(4.9);
            assertThat(repo.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("IOException al leer el Resource → IllegalStateException")
        void ioException_throwsIllegalState() throws Exception {
            Resource res = mock(Resource.class);
            when(res.getInputStream()).thenThrow(new java.io.IOException("boom"));

            assertThatThrownBy(() -> new SellerRepositoryImpl(res, mapper))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot load sellers data");
        }

        @Test
        @DisplayName("byId es inmutable (UnsupportedOperationException al mutar)")
        void byId_unmodifiable() {
            String json = """
        [
          {"id":"S-1","nickname":"A","reputation":4.0,"metrics":{"cancellations":0.01,"delays":0.02}}
        ]
        """;
            SellerRepositoryImpl repo = new SellerRepositoryImpl(bytes(json), mapper);

            Map<String, Seller> byIdView = repo.getById();
            assertThatThrownBy(() -> byIdView.put("NEW",
                    Seller.builder()
                            .id("NEW").nickname("N").reputation(1.0)
                            .metrics(Seller.Metrics.builder().cancellations(0).delays(0).build())
                            .build()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("findAll() expone vista no modificable")
        void findAll_unmodifiable() {
            String json = """
        [
          {"id":"S-1","nickname":"A","reputation":4.0,"metrics":{"cancellations":0.01,"delays":0.02}}
        ]
        """;
            SellerRepositoryImpl repo = new SellerRepositoryImpl(bytes(json), mapper);

            Collection<Seller> values = repo.findAll();
            assertThat(values).hasSize(1);

            assertThatThrownBy(() -> values.add(
                    Seller.builder()
                            .id("S-2").nickname("B").reputation(3.0)
                            .metrics(Seller.Metrics.builder().cancellations(0.02).delays(0.03).build())
                            .build()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}