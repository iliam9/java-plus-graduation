package ru.practicum.ewm.controller.event;

import feign.FeignException;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.EventServiceFeignClient;
import ru.practicum.ewm.dto.EventWithInitiatorDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.PublicSearchEventParams;
import ru.practicum.ewm.model.Sorting;
import ru.practicum.ewm.service.event.EventService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
public class PublicEventController implements EventServiceFeignClient {
    private final EventService eventService;

    @GetMapping
    public Collection<EventShortDto> findEvents(@RequestParam(required = false) String text,
                                                @RequestParam(required = false) List<@Positive Long> categories,
                                                @RequestParam(required = false) Boolean paid,
                                                @RequestParam(required = false) LocalDateTime rangeStart,
                                                @RequestParam(required = false) LocalDateTime rangeEnd,
                                                @RequestParam(required = false) boolean onlyAvailable,
                                                @RequestParam(required = false) Sorting sort,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        PublicSearchEventParams params = new PublicSearchEventParams(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
        log.info("Received public request to find events with params: {}", params);
        return eventService.findEventsPublic(params);
    }

    @GetMapping("/{id}")
    public EventFullDto findEventById(@Positive @PathVariable long id, @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("Received public request to find event with ID = {}", id);
        return eventService.findEventByIdPublic(id, userId);
    }

    @Override
    public EventWithInitiatorDto findEventWithInitiator(long eventId) throws FeignException {
        log.info("Received public request to find event with id: {}", eventId);
        return eventService.findBy(eventId);
    }
}
