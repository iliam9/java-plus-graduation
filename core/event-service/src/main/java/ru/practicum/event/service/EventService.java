package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    List<EventShortDto> getEventsForUser(EventDtoGetParam prm);

    EventFullDto getEventByIdForUser(EventDtoGetParam prm);

    List<EventFullDto> getEventsForAdmin(EventDtoGetParam prm);

    EventFullDto updateEventByAdmin(Long id, UpdateEventAdminRequest rq);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest rq);

    List<EventShortDto> getPublicEvents(EventDtoGetParam prm, HttpServletRequest rqt);

    EventFullDto getPublicEventById(Long id, Long userId, HttpServletRequest rqt);

    EventFullDto getEventById(Long id);

    List<Event> getAllEventByIds(List<Long> ids);

    List<ParticipationRequestDto> getAllByInitiatorIdAndEventId(long userId, long eventId);

    EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(EventRequestStatusUpdateRequest updateRequest,
                                                                        long userId, long eventId);

    void likeEvent(Long userId, Long eventId);

    List<EventShortDto> findRecommendation(Long userId);
}
