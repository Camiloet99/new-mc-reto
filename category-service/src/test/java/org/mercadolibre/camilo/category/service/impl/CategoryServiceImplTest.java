package org.mercadolibre.camilo.category.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mercadolibre.camilo.category.dto.BreadcrumbNode;
import org.mercadolibre.camilo.category.dto.CategoryResponse;
import org.mercadolibre.camilo.category.exception.CategoryNotFoundException;
import org.mercadolibre.camilo.category.exception.InvalidRequestException;
import org.mercadolibre.camilo.category.model.Category;
import org.mercadolibre.camilo.category.repository.CategoryRepository;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    CategoryRepository repo;

    private CategoryServiceImpl service() {
        return new CategoryServiceImpl(repo);
    }

    private Category cat(String id, String name, String parent) {
        return Category.builder().id(id).name(name).parentId(parent).build();
    }

    @Nested
    class GetWithDerived {
        @Test
        @DisplayName("Invalid id (blank) returns InvalidRequestException")
        void blankId() {
            StepVerifier.create(service().getWithDerived("  "))
                    .expectErrorSatisfies(ex -> assertThat(ex)
                            .isInstanceOf(InvalidRequestException.class)
                            .hasMessage("Category id must not be blank"))
                    .verify();
        }

        @Test
        @DisplayName("Not found id returns CategoryNotFoundException")
        void notFound() {
            when(repo.findById("x")).thenReturn(Optional.empty());
            StepVerifier.create(service().getWithDerived("x"))
                    .expectError(CategoryNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("Breadcrumb stops gracefully when parent is missing (orphan chain)")
        void orphanChain() {
            Category orphan = cat("orphan", "Orphan", "missingParent");
            when(repo.findById("orphan")).thenReturn(Optional.of(orphan));
            when(repo.findById("missingParent")).thenReturn(Optional.empty());
            when(repo.childrenOf("orphan")).thenReturn(List.of());

            StepVerifier.create(service().getWithDerived("orphan"))
                    .assertNext(resp -> {
                        assertThat(resp.getPathFromRoot()).extracting(BreadcrumbNode::getId)
                                .containsExactly("orphan");
                        assertThat(resp.getChildrenCount()).isZero();
                    })
                    .verifyComplete();
        }
    }

    @Nested
    class BreadcrumbOnly {
        @Test
        @DisplayName("Invalid id (null/blank) returns InvalidRequestException")
        void blank() {
            StepVerifier.create(service().breadcrumb(""))
                    .expectError(InvalidRequestException.class)
                    .verify();
        }

        @Test
        @DisplayName("Not found returns CategoryNotFoundException")
        void notFound() {
            when(repo.findById("abc")).thenReturn(Optional.empty());
            StepVerifier.create(service().breadcrumb("abc"))
                    .expectError(CategoryNotFoundException.class)
                    .verify();
        }

        @Test
        @DisplayName("Returns ordered breadcrumb from root to leaf")
        void success() {
            Category r = cat("r", "R", null);
            Category a = cat("a", "A", "r");
            Category b = cat("b", "B", "a");
            when(repo.findById("b")).thenReturn(Optional.of(b));
            when(repo.findById("a")).thenReturn(Optional.of(a));
            when(repo.findById("r")).thenReturn(Optional.of(r));

            StepVerifier.create(service().breadcrumb("b"))
                    .assertNext(path -> assertThat(path).extracting(BreadcrumbNode::getId)
                            .containsExactly("r", "a", "b"))
                    .verifyComplete();
        }
    }

    @Nested
    class FindAll {

        @Test
        @DisplayName("Parent not found returns CategoryNotFoundException")
        void parentNotFound() {
            when(repo.findById("nope")).thenReturn(Optional.empty());
            StepVerifier.create(service().findAll("nope"))
                    .expectError(CategoryNotFoundException.class)
                    .verify();
        }
    }
}
