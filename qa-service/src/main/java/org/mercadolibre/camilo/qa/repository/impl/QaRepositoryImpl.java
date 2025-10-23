package org.mercadolibre.camilo.qa.repository.impl;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mercadolibre.camilo.qa.exception.QaDataLoadException;
import org.mercadolibre.camilo.qa.model.Answer;
import org.mercadolibre.camilo.qa.model.Question;
import org.mercadolibre.camilo.qa.repository.QaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Repository
public class QaRepositoryImpl implements QaRepository {

    private final Map<String, Question> byId;
    private final Map<String, List<Question>> byProduct;

    /**
     * Crea el repositorio leyendo el JSON configurado y construyendo índices inmutables.
     * <ul>
     *   <li>Ordena preguntas por <b>fecha desc</b>.</li>
     *   <li>Ordena respuestas por <b>fecha asc</b>.</li>
     *   <li>Ignora registros inválidos (sin id o productId) y registra advertencia.</li>
     *   <li>Si hay ids duplicados de pregunta, prevalece el último (last-wins) y registra advertencia.</li>
     * </ul>
     *
     * @param data   Recurso JSON (prop: {@code app.data-path})
     * @param mapper ObjectMapper para deserialización
     * @throws QaDataLoadException si ocurre un error de lectura o parseo
     */
    public QaRepositoryImpl(@Value("${app.data-path}") Resource data, ObjectMapper mapper) {
        Objects.requireNonNull(data, "Resource 'data' must not be null");
        Objects.requireNonNull(mapper, "ObjectMapper must not be null");

        try (InputStream in = data.getInputStream()) {
            List<Question> raw = mapper.readValue(in, new TypeReference<List<Question>>() {
            });
            List<Question> normalized = raw.stream()
                    .map(q -> Question.builder()
                            .id(q.getId())
                            .productId(q.getProductId())
                            .author(q.getAuthor())
                            .text(q.getText())
                            .createdAt(q.getCreatedAt())
                            .answers(sortedAnswers(q.getAnswers()))
                            .build())
                    .sorted(Comparator.comparing(Question::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            Map<String, Question> idMap = new HashMap<>(Math.max(16, normalized.size() * 2));
            Map<String, List<Question>> prodMap = new HashMap<>();
            int invalid = 0;
            int duplicates = 0;

            for (int i = 0; i < normalized.size(); i++) {
                Question q = normalized.get(i);
                if (q == null || isBlank(q.getId()) || isBlank(q.getProductId())) {
                    invalid++;
                    log.warn("QaRepositoryImpl | skipping invalid record at index={} (missing id/productId)", i);
                    continue;
                }

                Question prev = idMap.put(q.getId(), q);
                if (prev != null) {
                    duplicates++;
                    log.warn("QaRepositoryImpl | duplicate question id='{}' at index={} (last-wins)", q.getId(), i);
                }
                prodMap.computeIfAbsent(q.getProductId(), k -> new ArrayList<>()).add(q);
            }

            this.byId = Collections.unmodifiableMap(idMap);
            this.byProduct = unmodifiableDeep(prodMap);

            log.info("Loaded {} questions ({} invalid, {} duplicates resolved last-wins) from {}",
                    idMap.size(), invalid, duplicates, safeDesc(data));

            if (idMap.isEmpty()) {
                log.warn("QaRepositoryImpl | no questions loaded from {}", safeDesc(data));
            }

        } catch (Exception e) {
            throw new QaDataLoadException("Cannot load QA data from " + safeDesc(data), e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Devuelve una lista inmutable de respuestas ordenadas por fecha ascendente.
     */
    private static List<Answer> sortedAnswers(List<Answer> answers) {
        if (answers == null || answers.isEmpty()) return List.of();
        List<Answer> sorted = answers.stream()
                .sorted(Comparator.comparing(Answer::getCreatedAt))
                .collect(Collectors.toList());
        return Collections.unmodifiableList(sorted);
    }

    /**
     * Hace inmutables todas las listas de preguntas por producto y el mapa contenedor.
     */
    private static Map<String, List<Question>> unmodifiableDeep(Map<String, List<Question>> src) {
        Map<String, List<Question>> out = new HashMap<>(src.size());
        for (Map.Entry<String, List<Question>> e : src.entrySet()) {
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
    public Optional<Question> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Question> findByProductId(String productId) {
        return byProduct.getOrDefault(productId, List.of());
    }
}