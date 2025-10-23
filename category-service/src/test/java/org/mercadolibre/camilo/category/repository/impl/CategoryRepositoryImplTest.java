package org.mercadolibre.camilo.category.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mercadolibre.camilo.category.exception.CategoriesDataLoadException;
import org.mercadolibre.camilo.category.model.Category;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link CategoryRepositoryImpl} focusing on constructor loading logic and public API behavior.
 */
class CategoryRepositoryImplTest {

    // Use an ObjectMapper configured like Spring Boot (register modules: parameter names, etc.)
    private ObjectMapper mapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    private Resource json(String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getDescription() {
                return "in-memory-json";
            }
        };
    }

    @Test
    @DisplayName("Loads valid categories and builds children index")
    void loadValidCategories_buildsIndices() {
        String data = """
                [
                  {"id":"root","name":"Root"},
                  {"id":"c1","name":"Child 1","parentId":"root"},
                  {"id":"c2","name":"Child 2","parentId":"root"}
                ]
                """;
        CategoryRepositoryImpl repo = new CategoryRepositoryImpl(json(data), mapper());

        assertThat(repo.findById("root")).isPresent();
        assertThat(repo.findById("c1")).isPresent();
        assertThat(repo.findById("c2")).isPresent();
        assertThat(repo.findById("missing")).isNotPresent();

        List<Category> children = repo.childrenOf("root");
        assertThat(children).hasSize(2).extracting(Category::getId).containsExactly("c1", "c2");

        assertThat(repo.childrenOf("unknown")).isEmpty();
    }

    @Test
    @DisplayName("childrenOf returns immutable empty list for unknown id")
    void childrenOfUnknown_isEmptyImmutable() {
        String data = "[]"; // empty dataset
        CategoryRepositoryImpl repo = new CategoryRepositoryImpl(json(data), mapper());

        List<Category> children = repo.childrenOf("nope");
        assertThat(children).isEmpty();
        assertThatThrownBy(() -> children.add(Category.builder().id("x").name("X").parentId("nope").build()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Duplicate ids resolved last-wins in byId map")
    void duplicates_lastWins() {
        String data = """
                [
                  {"id":"X","name":"First"},
                  {"id":"X","name":"Second"}
                ]
                """;
        CategoryRepositoryImpl repo = new CategoryRepositoryImpl(json(data), mapper());

        Category x = repo.findById("X").orElseThrow();
        assertThat(x.getName()).isEqualTo("Second"); // last entry wins
        assertThat(repo.getById()).hasSize(1);
    }

    @Test
    @DisplayName("Indices (byId and children lists) are unmodifiable")
    void indices_areUnmodifiable() {
        String data = """
                [
                  {"id":"root","name":"Root"},
                  {"id":"c1","name":"Child 1","parentId":"root"}
                ]
                """;
        CategoryRepositoryImpl repo = new CategoryRepositoryImpl(json(data), mapper());

        Map<String, Category> byId = repo.getById();
        assertThatThrownBy(() -> byId.put("new", Category.builder().id("new").name("New").build()))
                .isInstanceOf(UnsupportedOperationException.class);

        List<Category> children = repo.childrenOf("root");
        assertThat(children).hasSize(1);
        assertThatThrownBy(() -> children.add(Category.builder().id("c2").name("Child 2").parentId("root").build()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Malformed JSON triggers CategoriesDataLoadException")
    void malformedJson_throws() {
        String data = "[{"; // invalid JSON
        assertThatThrownBy(() -> new CategoryRepositoryImpl(json(data), mapper()))
                .isInstanceOf(CategoriesDataLoadException.class);
    }

    @Test
    @DisplayName("I/O error while reading resource triggers CategoriesDataLoadException")
    void ioError_throws() {
        Resource broken = new AbstractResource() {
            @Override
            public String getDescription() { return "broken-resource"; }
            @Override
            public InputStream getInputStream() throws IOException { throw new IOException("boom"); }
        };
        assertThatThrownBy(() -> new CategoryRepositoryImpl(broken, mapper()))
                .isInstanceOf(CategoriesDataLoadException.class);
    }

    @Test
    @DisplayName("Blank parentId values are ignored (not indexed as children)")
    void blankParentIdIgnored() {
        String data = """
                [
                  {"id":"root","name":"Root"},
                  {"id":"cBlank","name":"Child Blank","parentId":""},
                  {"id":"cSpaces","name":"Child Spaces","parentId":"  "}
                ]
                """;
        CategoryRepositoryImpl repo = new CategoryRepositoryImpl(json(data), mapper());
        assertThat(repo.childrenOf("root")).isEmpty();
        assertThat(repo.getById()).hasSize(3);
    }
}
