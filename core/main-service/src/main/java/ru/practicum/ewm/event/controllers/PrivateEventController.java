package ru.practicum.ewm.event.controllers;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.service.EventService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping("/{userId}/events")
    public Collection<EventShortDto> findAllEvents(@Positive @PathVariable long userId,
                                                   @RequestParam(defaultValue = "0") long from,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   HttpServletRequest request) {
        log.info("Request to find user events {}", userId);
        PrivateSearchEventDto paramEventsDto = new PrivateSearchEventDto(userId, from, size, request.getRemoteAddr());
        return eventService.findBy(paramEventsDto);
    }

    @PostMapping("/{userId}/events")
    @ResponseStatus(value = HttpStatus.CREATED)
    public EventFullDto createEvents(@Positive @PathVariable long userId,
                                     @Valid @RequestBody NewEventDto newEvent) {
        log.info("Request to create event {} by user {}", newEvent,userId);
        return eventService.create(userId, newEvent);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto findEvent(@Positive @PathVariable long userId,
                                  @Positive @PathVariable long eventId,
                                  HttpServletRequest request) {
        ParamEventDto paramEventDto = new ParamEventDto(userId, eventId);
        String remoteAddr = request.getRemoteAddr();
        log.info("Request to find event {}", paramEventDto);
        return eventService.findBy(paramEventDto,remoteAddr);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEvent(@Positive @PathVariable long userId,
                                    @Positive @PathVariable long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateEvent) {
        ParamEventDto paramEventDto = new ParamEventDto(userId, eventId);
        log.info("Request to update event {}, {}", paramEventDto, updateEvent);
        return eventService.update(paramEventDto, updateEvent);
    }
}
