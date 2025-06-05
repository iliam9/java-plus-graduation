package ru.practicum.ewm.service.event;

import ru.practicum.ewm.dto.EventWithInitiatorDto;
import ru.practicum.ewm.dto.event.AdminSearchEventDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.PrivateSearchEventDto;
import ru.practicum.ewm.dto.event.PublicSearchEventParams;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.dto.ParamEventDto;

import java.util.Collection;

public interface EventService {
    Collection<EventShortDto> findBy(PrivateSearchEventDto privateSearchEventDto);

    EventFullDto findBy(ParamEventDto paramEventDto, String ip);

    Collection<EventFullDto> findEventsAdmin(AdminSearchEventDto adminSearchEventDto);

    EventFullDto findEventByIdPublic(long id, String ip);

    Collection<EventShortDto> findEventsPublic(PublicSearchEventParams params);

    EventFullDto create(long userId, NewEventDto newEvent);

    EventFullDto update(ParamEventDto paramEventDto, UpdateEventUserRequest updateEvent);

    EventFullDto update(long eventId, UpdateEventUserRequest updateEvent);

    EventWithInitiatorDto findBy(long eventId);
}
