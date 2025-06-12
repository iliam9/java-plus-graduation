package ru.practicum.event.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;

import java.util.List;

@FeignClient(name = "event-service", fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient {

    @GetMapping("/admin/events")
    List<EventFullDto> getEvents(@SpringQueryMap @Valid EventDtoGetParam prm);

    @PatchMapping("/admin/events/{eventId}")
    EventFullDto updateEvent(@PathVariable Long eventId, @RequestBody @Valid UpdateEventAdminRequest rq);

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    EventFullDto addEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto);

    @GetMapping("/users/{userId}/events")
    List<EventShortDto> getEventsForUser(@SpringQueryMap EventDtoGetParam prm);

    @GetMapping("/users/{userId}/events/{eventId}")
    EventFullDto getEventForUserById(@PathVariable Long userId, @PathVariable Long eventId,
                                     @SpringQueryMap EventDtoGetParam prm);

    @PatchMapping("/users/{userId}/events/{eventId}")
    EventFullDto updateEvent(@PathVariable Long userId, @PathVariable Long eventId,
                             @RequestBody @Valid UpdateEventUserRequest rq);

    @GetMapping("/events")
    List<EventShortDto> getPublicEvents(@SpringQueryMap EventDtoGetParam prm);

    @GetMapping("/events/{id}")
    EventFullDto getPublicEventById(@PathVariable Long id);

    @GetMapping("/admin/events/{eventId}")
    EventFullDto getEvent(@PathVariable Long eventId);
}
