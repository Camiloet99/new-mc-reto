package com.mercadolibre.camilo.service;

import com.mercadolibre.camilo.dto.SellerResponse;
import com.mercadolibre.camilo.exceptions.InvalidRequestException;
import com.mercadolibre.camilo.exceptions.SellerNotFoundException;
import com.mercadolibre.camilo.model.Seller;
import com.mercadolibre.camilo.repository.SellerRepository;
import com.mercadolibre.camilo.service.impl.SellerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceImplTest {

    @Mock
    SellerRepository repo;

    @InjectMocks
    SellerServiceImpl serviceImpl;

    SellerService service;

    Seller seller1;
    Seller seller2;

    @BeforeEach
    void setUp() {
        service = serviceImpl;

        seller1 = Seller.builder()
                .id("S-50")
                .nickname("TechStore")
                .reputation(4.7)
                .metrics(Seller.Metrics.builder().cancellations(0.03).delays(0.10).build())
                .build();

        seller2 = Seller.builder()
                .id("S-77")
                .nickname("CasaGadgets")
                .reputation(3.9)
                .metrics(Seller.Metrics.builder().cancellations(0.05).delays(0.08).build())
                .build();
    }

    @Nested
    @DisplayName("get(id)")
    class GetById {

        @Test
        @DisplayName("debe fallar con InvalidRequestException cuando id es null")
        void get_nullId_throwsInvalidRequest() {
            Mono<SellerResponse> mono = service.get(null);

            StepVerifier.create(mono)
                    .expectErrorSatisfies(err -> {
                        assertThat(err).isInstanceOf(InvalidRequestException.class);
                        assertThat(err).hasMessage("Seller id must not be blank");
                    })
                    .verify();

            verifyNoInteractions(repo);
        }

        @Test
        @DisplayName("debe fallar con InvalidRequestException cuando id es blank")
        void get_blankId_throwsInvalidRequest() {
            Mono<SellerResponse> mono = service.get("   ");

            StepVerifier.create(mono)
                    .expectErrorSatisfies(err -> {
                        assertThat(err).isInstanceOf(InvalidRequestException.class);
                        assertThat(err).hasMessage("Seller id must not be blank");
                    })
                    .verify();

            verifyNoInteractions(repo);
        }

        @Test
        @DisplayName("debe fallar con SellerNotFoundException cuando no existe")
        void get_notFound_throwsSellerNotFound() {
            when(repo.findById("S-404")).thenReturn(Optional.empty());

            Mono<SellerResponse> mono = service.get("S-404");

            StepVerifier.create(mono)
                    .expectErrorSatisfies(err -> {
                        assertThat(err).isInstanceOf(SellerNotFoundException.class);
                        assertThat(err.getMessage()).isEqualTo("Seller 'S-404' not found");
                    })
                    .verify();

            verify(repo, times(1)).findById("S-404");
            verifyNoMoreInteractions(repo);
        }

        @Test
        @DisplayName("debe mapear correctamente cuando existe")
        void get_ok_mapsToResponse() {
            when(repo.findById("S-50")).thenReturn(Optional.of(seller1));

            Mono<SellerResponse> mono = service.get("S-50");

            StepVerifier.create(mono)
                    .assertNext(resp -> {
                        assertThat(resp.getId()).isEqualTo("S-50");
                        assertThat(resp.getNickname()).isEqualTo("TechStore");
                        assertThat(resp.getReputation()).isEqualTo(4.7);
                        assertThat(resp.getMetrics().getCancellations()).isEqualTo(0.03);
                        assertThat(resp.getMetrics().getDelays()).isEqualTo(0.10);
                    })
                    .verifyComplete();

            verify(repo, times(1)).findById("S-50");
            verifyNoMoreInteractions(repo);
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("debe devolver la lista mapeada cuando hay datos")
        void findAll_ok_returnsMappedList() {
            when(repo.findAll()).thenReturn(List.of(seller1, seller2));

            Flux<SellerResponse> flux = service.findAll();

            StepVerifier.create(flux)
                    .recordWith(java.util.ArrayList::new)
                    .expectNextCount(2)
                    .consumeRecordedWith(col -> {
                        var list = new java.util.ArrayList<>(col);
                        SellerResponse r1 = list.get(0);
                        SellerResponse r2 = list.get(1);

                        assertThat(list).hasSize(2);
                        assertThat(r1.getId()).isEqualTo("S-50");
                        assertThat(r2.getId()).isEqualTo("S-77");
                    })
                    .verifyComplete();

            verify(repo, times(1)).findAll();
            verifyNoMoreInteractions(repo);
        }

        @Test
        @DisplayName("debe completar vacío cuando no hay datos")
        void findAll_empty_completes() {
            when(repo.findAll()).thenReturn(List.of());

            Flux<SellerResponse> flux = service.findAll();

            StepVerifier.create(flux)
                    .expectNextCount(0)
                    .verifyComplete();

            verify(repo, times(1)).findAll();
            verifyNoMoreInteractions(repo);
        }
    }
}