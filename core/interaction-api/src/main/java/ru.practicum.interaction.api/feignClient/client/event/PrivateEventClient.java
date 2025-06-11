package ru.practicum.interaction.api.feignClient.client.event;
import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.dto.event.*;
import ru.practicum.interaction.api.dto.request.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "event-service", path = "/users/{userId}/events")
public interface PrivateEventClient {

    @GetMapping
    List<EventShortDto> getEventsOfUser(@PathVariable Long userId,
                                        @RequestParam(defaultValue = "0") Integer from,
                                        @RequestParam(defaultValue = "10") Integer size) throws FeignException;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto addEvent(@Valid @RequestBody NewEventDto eventDto, @PathVariable Long userId) throws FeignException;

    @GetMapping("/{eventId}")
    EventFullDto getEventOfUser(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;

    @PatchMapping("/{eventId}")
    EventFullDto updateEventOfUser(@Valid @RequestBody UpdateEventUserRequest updateRequest,
                                          @PathVariable("userId") Long userId,
                                          @PathVariable Long eventId) throws FeignException;

    @GetMapping("/{eventId}/requests")
    List<ParticipationRequestDto> getRequestsOfUserEvent(@PathVariable Long userId, @PathVariable Long eventId) throws FeignException;
}
