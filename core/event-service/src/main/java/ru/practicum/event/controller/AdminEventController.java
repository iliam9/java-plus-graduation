package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.events.EventDto;
import ru.practicum.event.dto.EventAdminUpdateDto;
import ru.practicum.event.dto.SearchEventsParam;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    public static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;


    /**
     * Получить список мероприятий с конкретыми параметрами.
     *
     * @param users      идентификаторы пользователей, создавших запрашиваемые мероприятия
     * @param categories идентификаторы категорий (разделов) к которым должны принадлежать запрашиваемые мероприятия
     * @param states     статус запрашиваемых мероприятий
     * @param rangeStart начало временного промежутка в который должны проводиться запрашиваемые мероприятия
     * @param rangeEnd   конец временного промежутка в который должны проводиться запрашиваемые мероприятия
     * @param from       смещение от начала возвращаемого списка мероприятий
     * @param size       размер возвращаемого списка мероприятий
     * @return список запрашиваемых мероприятий с конкретными параметрами
     */
    @GetMapping
    public List<EventDto> searchEvents(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) List<EventState> states,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(name = "from", required = false, defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
        return eventService.adminEventsSearch(new SearchEventsParam(users, categories, states, rangeStart, rangeEnd, from, size));
    }

    /**
     * Обновить информацию о существующем мероприятии.
     *
     * @param eventId  идентификатор мероприятия
     * @param eventDto представление обновляемого мероприятия
     * @return представление обновленного мероприятия
     */
    @PatchMapping(path = "{eventId}")
    public EventDto editEvent(@PathVariable("eventId") Long eventId, @Valid @RequestBody final EventAdminUpdateDto eventDto) {
        return eventService.adminEventUpdate(eventId, eventDto);
    }

}
