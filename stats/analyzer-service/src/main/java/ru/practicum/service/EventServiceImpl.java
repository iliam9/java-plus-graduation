package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.mapping.EventMapper;
import ru.practicum.model.EventAction;
import ru.practicum.model.EventActionId;
import ru.practicum.repository.EventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public void update(EventSimilarityAvro eventSimilarityAvro) {
        log.info("Save EventSimilarity: {}", eventSimilarityAvro);
        EventAction eventAction;
        EventAction existEvent = getById(eventSimilarityAvro.getEventA(), eventSimilarityAvro.getEventB());
        if (existEvent == null) {
            eventAction = eventRepository.save(EventMapper.map(eventSimilarityAvro));
        }  else {
            existEvent.setTimestamp(eventSimilarityAvro.getTimestamp());
            existEvent.setScore(eventSimilarityAvro.getScore());
            eventAction = eventRepository.save(existEvent);
        }
        log.info("Saved EventAction: {}", eventAction);
    }

    private EventAction getById(long eventA, long eventB) {
        EventActionId eventActionId = new EventActionId();
        eventActionId.setEventA(eventA);
        eventActionId.setEventB(eventB);
        return eventRepository.findById(eventActionId).orElse(null);
    }
}