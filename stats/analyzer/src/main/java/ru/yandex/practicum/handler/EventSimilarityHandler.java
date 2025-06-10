package ru.yandex.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.mapper.AnalyzerMapper;
import ru.yandex.practicum.model.EventSimilarity;
import ru.yandex.practicum.repository.EventSimilarityRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityHandler {
    private final AnalyzerMapper mapper;
    private final EventSimilarityRepository repository;

    public void handle(EventSimilarityAvro eventSimilarityAvro) {
        EventSimilarity eventSimilarity = mapper.map(eventSimilarityAvro);
        repository.save(eventSimilarity);
        log.info("Saved similarity for events A = {} and B = {}",
                eventSimilarityAvro.getEventA(),
                eventSimilarityAvro.getEventB());
    }
}