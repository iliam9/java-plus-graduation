package ru.practicum.mapping;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.model.EventAction;
import ru.practicum.model.EventActionId;

public class EventMapper {

    public static EventAction map(EventSimilarityAvro eventSimilarityAvro) {
        EventAction eventAction = new EventAction();
        EventActionId eventActionId = new EventActionId();
        eventActionId.setEventA(eventSimilarityAvro.getEventA());
        eventActionId.setEventB(eventSimilarityAvro.getEventB());
        eventAction.setId(eventActionId);
        eventAction.setScore(eventSimilarityAvro.getScore());
        eventAction.setTimestamp(eventSimilarityAvro.getTimestamp());
        return eventAction;
    }
}
