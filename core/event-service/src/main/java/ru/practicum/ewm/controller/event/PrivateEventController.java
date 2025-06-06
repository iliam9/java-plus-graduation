package ru.practicum.ewm.controller.event;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.PrivateSearchEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.service.event.EventService;
import ru.practicum.ewm.dto.ParamEventDto;

import java.util.Collection;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateEventController {

    private final EventService eventService;

    @GetMapping("/{userId}/events")
    public Collection<EventShortDto> findAllEvents(@Positive @PathVariable long userId,
                                                   @RequestParam(defaultValue = "0") int from,
                                                   @RequestParam(defaultValue = "10") int size,
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
