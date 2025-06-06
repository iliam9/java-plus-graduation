package ru.practicum.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.EventSort;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.ewm.EndpointHitInputDto;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.ViewStatsOutputDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicEventServiceImpl implements PublicEventService {
    private static final String APP_NAME = "ewm-main";
    private static final String EVENTS_URI_PREFIX = "/events/";
    private static final Duration STATS_QUERY_DURATION = Duration.ofDays(1);

    private final EventRepository eventRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventById(long id, HttpServletRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id " + id + " not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id " + id + " is not published yet");
        }

        addHit(request);
        updateEventViews(event);

        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         EventSort sort,
                                         int from,
                                         int size,
                                         HttpServletRequest request) {
        validatePaginationParams(from, size);
        validateTimeRange(rangeStart, rangeEnd);

        PageRequest page = PageRequest.of(from / size, size, getSort(sort));
        Page<Event> pageEvents = getFilteredEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, page);

        List<Event> events = pageEvents.getContent();
        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        addHit(request);
        updateEventsViews(events);

        List<EventShortDto> eventShortDtos = eventMapper.toEventShortDto(events);
        if (sort == EventSort.VIEWS) {
            eventShortDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return eventShortDtos;
    }

    private Sort getSort(EventSort sort) {
        return sort == EventSort.EVENT_DATE
                ? Sort.by("eventDate").ascending()
                : Sort.unsorted();
    }

    private void validateTimeRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new BadRequestException("End date cannot be before start date");
        }
    }

    private Page<Event> getFilteredEvents(String text,
                                          List<Long> categories,
                                          Boolean paid,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          Boolean onlyAvailable,
                                          PageRequest page) {
        LocalDateTime start = Optional.ofNullable(rangeStart).orElse(LocalDateTime.now());
        LocalDateTime end = Optional.ofNullable(rangeEnd).orElse(start.plusYears(1));

        if (onlyAvailable) {
            return eventRepository.findAllByPublicFiltersAndOnlyAvailable(
                    text, categories, paid, start, end, page);
        }
        return eventRepository.findAllByPublicFilters(text, categories, paid, start, end, page);
    }

    private void updateEventsViews(List<Event> events) {
        List<String> eventUris = events.stream()
                .map(event -> EVENTS_URI_PREFIX + event.getId())
                .toList();

        Map<Long, Long> eventViews = getEventsViews(eventUris);

        events.forEach(event -> {
            Long views = eventViews.getOrDefault(event.getId(), 0L);
            event.setViews(views);
        });

        eventRepository.saveAll(events);
    }

    private Map<Long, Long> getEventsViews(List<String> uris) {
        try {
            ResponseEntity<Object> response = statsClient.getStats(
                    LocalDateTime.now().minus(STATS_QUERY_DURATION),
                    LocalDateTime.now().plusHours(1),
                    uris,
                    true);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<ViewStatsOutputDto> stats = objectMapper.convertValue(
                        response.getBody(),
                        new TypeReference<>() {});

                return stats.stream()
                        .filter(stat -> stat.getUri() != null)
                        .collect(Collectors.toMap(
                                stat -> parseEventIdFromUri(stat.getUri()),
                                ViewStatsOutputDto::getHits,
                                (existing, replacement) -> existing
                        ));
            }
        } catch (Exception e) {
            log.error("Failed to get views stats", e);
        }
        return Collections.emptyMap();
    }

    private Long parseEventIdFromUri(String uri) {
        try {
            return Long.parseLong(uri.substring(EVENTS_URI_PREFIX.length()));
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            log.error("Failed to parse event ID from URI: {}", uri, e);
            return -1L;
        }
    }

    private void updateEventViews(Event event) {
        String eventUri = EVENTS_URI_PREFIX + event.getId();
        Map<Long, Long> views = getEventsViews(List.of(eventUri));
        event.setViews(views.getOrDefault(event.getId(), 0L));
        eventRepository.save(event);
    }

    private void addHit(HttpServletRequest request) {
        if (request == null) return;

        EndpointHitInputDto hit = new EndpointHitInputDto();
        hit.setApp(APP_NAME);
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now());

        try {
            statsClient.addHit(hit);
        } catch (Exception e) {
            log.error("Failed to send hit to stats service", e);
        }
    }

    private void validatePaginationParams(int from, int size) {
        if (from < 0) {
            throw new ValidationException("Parameter 'from' cannot be negative");
        }
        if (size <= 0) {
            throw new ValidationException("Parameter 'size' must be positive");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getEventFullById(long id) {
        return eventRepository.findById(id);
    }
}