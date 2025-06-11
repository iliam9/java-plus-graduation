package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventsSimilarity;
import ru.practicum.repository.EventsSimilarityRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventsSimilarityHandler {

    private final EventsSimilarityRepository eventsSimilarityRepository;

    public void handleEventsSimilarity(EventSimilarityAvro eventSimilarityAvro) {
        EventsSimilarity eventsSimilarity = EventsSimilarity.builder()
                .eventAId( eventSimilarityAvro.getEventA())
                .eventBId( eventSimilarityAvro.getEventB())
                .similarityScore(eventSimilarityAvro.getScore())
                .timestamp(LocalDateTime.ofInstant(eventSimilarityAvro.getTimestamp(), ZoneId.of("UTC")))
                .build();

        Optional<EventsSimilarity> existedEventsSimilarity = eventsSimilarityRepository.findByEventAIdAndEventBId(
                eventsSimilarity.getEventAId(), eventsSimilarity.getEventBId());

        if (existedEventsSimilarity.isPresent()) {
            EventsSimilarity existed = existedEventsSimilarity.get();
            existed.setSimilarityScore(eventsSimilarity.getSimilarityScore());
            existed.setTimestamp(eventsSimilarity.getTimestamp());
            eventsSimilarityRepository.save(existed);
        } else {
            eventsSimilarityRepository.save(eventsSimilarity);
        }
    }
}
