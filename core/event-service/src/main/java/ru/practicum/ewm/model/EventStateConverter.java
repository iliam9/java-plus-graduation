package ru.practicum.ewm.model;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.EventState;


@Component
public class EventStateConverter implements Converter<String, EventState> {
    @Override
    public EventState convert(String source) {
        return EventState.valueOf(source.toUpperCase());
    }
}


