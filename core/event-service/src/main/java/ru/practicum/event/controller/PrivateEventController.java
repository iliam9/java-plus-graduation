package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId,
                                 @RequestBody @Valid NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getEventsForUser(@ModelAttribute EventDtoGetParam prm) {
        return eventService.getEventsForUser(prm);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventForUserById(@ModelAttribute EventDtoGetParam prm) {
        return eventService.getEventByIdForUser(prm);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @RequestBody @Valid UpdateEventUserRequest rq) {
        return eventService.updateEventByUser(userId, eventId, rq);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllEventsOfInitiator(@PathVariable(name = "userId") @Positive long userId,
                                                                 @PathVariable(name = "eventId") @Positive long eventId) {
        return eventService.getAllByInitiatorIdAndEventId(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult changeEventRequestsStatus(@PathVariable(name = "userId") @Positive long userId,
                                                                    @PathVariable(name = "eventId") @Positive long eventId,
                                                                    @RequestBody @Valid EventRequestStatusUpdateRequest updateRequest) {
        return eventService.changeEventRequestsStatusByInitiator(updateRequest, userId, eventId);
    }
}
