package ru.practicum.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventService {

    void update(EventSimilarityAvro eventSimilarityAvro);
}
