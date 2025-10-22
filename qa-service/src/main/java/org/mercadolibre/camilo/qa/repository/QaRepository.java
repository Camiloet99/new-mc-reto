package org.mercadolibre.camilo.qa.repository;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.qa.model.Answer;
import org.mercadolibre.camilo.qa.model.Question;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class QaRepository {

    @Getter
    private final Map<String, Question> byId;

    @Getter
    private final Map<String, List<Question>> byProduct;

    public QaRepository(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        try (InputStream in = data.getInputStream()) {
            List<Question> list = mapper.readValue(in, new TypeReference<List<Question>>() {});
            // sort preguntas por fecha desc; y respuestas por fecha asc (opcional)
            list = list.stream()
                    .map(q -> Question.builder()
                            .id(q.getId()).productId(q.getProductId()).author(q.getAuthor())
                            .text(q.getText()).createdAt(q.getCreatedAt())
                            .answers(sortedAnswers(q.getAnswers()))
                            .build())
                    .sorted(Comparator.comparing(Question::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            Map<String, Question> idMap = new HashMap<>();
            Map<String, List<Question>> prodMap = new HashMap<>();
            for (Question q : list) {
                idMap.put(q.getId(), q);
                prodMap.computeIfAbsent(q.getProductId(), k -> new ArrayList<>()).add(q);
            }
            this.byId = Collections.unmodifiableMap(idMap);
            this.byProduct = Collections.unmodifiableMap(prodMap);
            log.info("Loaded {} questions (qa)", list.size());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load QA data", e);
        }
    }

    private static List<Answer> sortedAnswers(List<Answer> answers) {
        if (answers == null) return List.of();
        return answers.stream()
                .sorted(Comparator.comparing(Answer::getCreatedAt)) // asc, primero pregunta, luego respuestas cronológicas
                .collect(Collectors.toList());
    }

    public Optional<Question> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Question> findByProductId(String productId) {
        return byProduct.getOrDefault(productId, List.of());
    }
}