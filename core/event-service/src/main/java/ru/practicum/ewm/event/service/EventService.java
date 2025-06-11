package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import ru.practicum.interaction.api.dto.event.*;
import ru.practicum.interaction.api.dto.request.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    EventFullDto addEvent(NewEventDto eventDto, Long userId);

    List<EventShortDto> getEventsOfUser(Long userId, Integer from, Integer size);

    EventFullDto getEventOfUser(Long userId, Long eventId);

    EventFullDto updateEventOfUser(UpdateEventUserRequest updateRequest, Long userId, Long eventId);

    List<EventShortDto> getPublicEventsByFilter(HttpServletRequest httpServletRequest, EventPublicFilter inputFilter);

    EventFullDto getPublicEventById(Long userId, Long id);

    List<EventFullDto> getEventsForAdmin(EventAdminFilter admin);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    List<ParticipationRequestDto> getRequestsOfUserEvent(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest updateRequest, Long userId, Long eventId);

    EventFullDto getEventById(@Positive Long id);

    void setConfirmedRequests(Long eventId, Integer count);

    EventFullDto getAdminEventById(@Positive Long id);

    List<EventShortDto> getEventsRecommendations(Long userId, int maxResults);

    void addLikeToEvent(Long eventId, Long userId);
}
