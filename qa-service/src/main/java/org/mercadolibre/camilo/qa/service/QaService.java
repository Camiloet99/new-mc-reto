package org.mercadolibre.camilo.qa.service;

import lombok.RequiredArgsConstructor;
import org.mercadolibre.camilo.qa.dto.QuestionResponse;
import org.mercadolibre.camilo.qa.exception.NotFoundException;
import org.mercadolibre.camilo.qa.repository.QaRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class QaService {

    private final QaRepository repo;

    public Flux<QuestionResponse> listByProduct(String productId) {
        return Flux.fromIterable(repo.findByProductId(productId))
                .map(QuestionResponse::from);
    }

    public Mono<QuestionResponse> get(String questionId) {
        return Mono.justOrEmpty(repo.findById(questionId))
                .switchIfEmpty(Mono.error(new NotFoundException("Question '%s' not found".formatted(questionId))))
                .map(QuestionResponse::from);
    }
}
