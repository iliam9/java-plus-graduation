package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.EventDto;
import ru.practicum.event.dto.EventCreateDto;
import ru.practicum.event.dto.EventUpdateDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
public class PrivateEventController {

    private final EventService eventService;


    /**
     * Получить список мероприятий, созданных конкретным пользователем.
     *
     * @param userId идентификатор пользователя, мероприятия которого запрашиваются
     * @param from   смещение от начала возвращаемого списка мероприятий
     * @param size   размер возвращаемого списка мероприятий
     * @return список мероприятий, созданных конкретным пользователем
     */
    @GetMapping
    public List<EventDto> getEvents(@PathVariable("userId") Long userId,
                                    @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                    @RequestParam(defaultValue = "10") int size) {
        return eventService.privateUserEvents(userId, from, size);
    }

    /**
     * Добавить новое мероприятия в систему.
     *
     * @param userId   идентификатор пользователя, создающего меропориятие
     * @param eventDto представление добавляемого мероприятия
     * @return представление добавленного мероприятия
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto createEvent(@PathVariable("userId") Long userId, @Valid @RequestBody EventCreateDto eventDto) {
        return eventService.privateEventCreate(userId, eventDto);
    }

    /**
     * Получить информацию о конкретном мероприятии по его идентификатору.
     *
     * @param userId  идентификатор пользователя, запрашивающего информацию о мероприятии
     * @param eventId идентификатор мероприятия
     * @return представление запрошенного мероприятия
     */
    @GetMapping(path = "/{eventId}")
    public EventDto getEvent(@PathVariable("userId") Long userId, @PathVariable("eventId") Long eventId) {
        return eventService.privateGetUserEvent(userId, eventId);
    }

    /**
     * Обновить информацию о существующем мероприятии.
     *
     * @param userId   идентификатор пользователя, обновляющего информацию о мероприятии
     * @param eventId  идентификатор мероприятия
     * @param eventDto представление обновляемого мероприятия
     * @return представление обновленного мероприятия
     */
    @PatchMapping(path = "/{eventId}")
    public EventDto updateEvent(@PathVariable("userId") Long userId,
                                @PathVariable("eventId") Long eventId, @Valid @RequestBody EventUpdateDto eventDto) {
        return eventService.privateUpdateUserEvent(userId, eventId, eventDto);
    }

}