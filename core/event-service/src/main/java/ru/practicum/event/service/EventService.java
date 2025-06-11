package ru.practicum.event.service;

import ru.practicum.dto.events.EventDto;
import ru.practicum.event.dto.*;

import java.util.List;

public interface EventService {

    List<EventDto> adminEventsSearch(SearchEventsParam searchEventsParam);

    EventDto adminEventUpdate(Long eventId, EventAdminUpdateDto eventDto);

    List<EventDto> privateUserEvents(Long userId, int from, int size);

    EventDto privateEventCreate(Long userId, EventCreateDto eventCreateDto);

    EventDto privateGetUserEvent(Long userId, Long eventId);

    EventDto privateUpdateUserEvent(Long userId, Long eventId, EventUpdateDto eventUpdateDto);

    List<EventShortDto> getEvents(EntityParam params);

    EventDto getEvent(Long userId, Long eventId);

    List<EventDto> getEvents(List<Long> ids);

    List<EventDto> getRecommendedEventsForUser(Long userId);

    void addLikeToEvent(Long userId, Long eventId);

    List<EventDto> findAllByCategoryId(Long categoryId);

    EventDto findByIdAndInitiatorId(Long eventId, Long userId);

    List<EventDto> findAllByInitiatorId(Long initiatorId);

}
