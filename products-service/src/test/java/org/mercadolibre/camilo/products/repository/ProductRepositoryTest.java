package org.mercadolibre.camilo.products.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mercadolibre.camilo.products.exception.ProductsDataLoadException;
import org.mercadolibre.camilo.products.model.Product;
import org.mercadolibre.camilo.products.repository.impl.ProductRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductRepositoryImplTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static Resource bytes(String json) {
        return new ByteArrayResource(json.getBytes()) {
            @Override public String getDescription() { return "in-memory-json"; }
        };
    }

    @Nested
    @DisplayName("Carga y consultas")
    class LoadAndQuery {

        @Test
        @DisplayName("debe cargar productos y permitir findById / findAll")
        void load_ok_and_query() {
            String json = """
        [
          {"id":"P-1"},
          {"id":"P-2"}
        ]
        """;
            Resource res = bytes(json);

            ProductRepositoryImpl repo = new ProductRepositoryImpl(res, mapper);

            Optional<Product> p1 = repo.findById("P-1");
            assertThat(p1).isPresent();
            assertThat(p1.get().getId()).isEqualTo("P-1");

            Collection<Product> all = repo.findAll();
            assertThat(all).hasSize(2);
            assertThat(all.stream().map(Product::getId)).containsExactlyInAnyOrder("P-1", "P-2");
        }

        @Test
        @DisplayName("IDs duplicados → el último prevalece (last-wins)")
        void duplicates_lastWins() {
            // JSON VÁLIDO (sin comentarios)
            String json = """
        [
          {"id":"P-1"},
          {"id":"P-1"}
        ]
        """;
            Resource res = bytes(json);

            ProductRepositoryImpl repo = new ProductRepositoryImpl(res, mapper);

            Optional<Product> p1 = repo.findById("P-1");
            assertThat(p1).isPresent();
            assertThat(repo.findAll()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Errores de carga")
    class LoadErrors {

        @Test
        @DisplayName("IOException al leer el Resource → ProductsDataLoadException")
        void ioException_throws() throws Exception {
            Resource res = mock(Resource.class);
            when(res.getInputStream()).thenThrow(new java.io.IOException("boom"));

            assertThatThrownBy(() -> new ProductRepositoryImpl(res, mapper))
                    .isInstanceOf(ProductsDataLoadException.class)
                    .hasMessageContaining("Cannot load products data");
        }
    }

    @Nested
    @DisplayName("Inmutabilidad")
    class Immutability {

        @Test
        @DisplayName("byId es inmutable (UnsupportedOperationException al mutar)")
        void byId_isUnmodifiable() {
            String json = """
        [
          {"id":"P-1"}
        ]
        """;
            ProductRepositoryImpl repo = new ProductRepositoryImpl(bytes(json), mapper);

            Map<String, Product> byIdView = repo.getById(); // expuesto por @Getter
            assertThatThrownBy(() -> byIdView.put("NEW", new Product())) // ajusta ctor según tu modelo
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("findAll() expone vista no modificable")
        void findAll_isUnmodifiable() {
            String json = """
        [
          {"id":"P-1"}
        ]
        """;
            ProductRepositoryImpl repo = new ProductRepositoryImpl(bytes(json), mapper);

            Collection<Product> values = repo.findAll();
            assertThat(values).hasSize(1);

            assertThatThrownBy(() -> values.add(new Product())) // ajusta ctor según tu modelo
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}