package org.mercadolibre.camilo.qa.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.qa.dto.QuestionResponse;
import org.mercadolibre.camilo.qa.exception.InvalidRequestException;
import org.mercadolibre.camilo.qa.exception.QuestionNotFoundException;
import org.mercadolibre.camilo.qa.repository.QaRepository;
import org.mercadolibre.camilo.qa.service.QaService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class QaServiceImpl implements QaService {

    private final QaRepository repo;

    @Override
    public Flux<QuestionResponse> listByProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            log.warn("QaService.listByProduct | invalid productId (blank)");
            return Flux.error(new InvalidRequestException("productId must not be blank"));
        }

        log.info("QaService.listByProduct | productId={}", productId);
        return Flux.fromIterable(repo.findByProductId(productId))
                .map(QuestionResponse::from)
                .doOnComplete(() -> log.debug("QaService.listByProduct | completed | productId={}", productId))
                .doOnError(ex -> log.error("QaService.listByProduct | error | productId={} | type={} | msg={}",
                        productId, ex.getClass().getSimpleName(), ex.getMessage()));
    }

    @Override
    public Mono<QuestionResponse> get(String questionId) {
        if (questionId == null || questionId.isBlank()) {
            log.warn("QaService.get | invalid questionId (blank)");
            return Mono.error(new InvalidRequestException("questionId must not be blank"));
        }

        log.info("QaService.get | fetching | questionId={}", questionId);
        return Mono.defer(() -> Mono.justOrEmpty(repo.findById(questionId)))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("QaService.get | not found | questionId={}", questionId);
                    return Mono.error(new QuestionNotFoundException(questionId));
                }))
                .map(QuestionResponse::from)
                .doOnError(ex -> log.error("QaService.get | error | questionId={} | type={} | msg={}",
                        questionId, ex.getClass().getSimpleName(), ex.getMessage()));
    }
}
