package ru.practicum.ewm.service.event;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.StatClient;
import ru.practicum.ewm.dto.EventWithInitiatorDto;
import ru.practicum.ewm.dto.HitDto;
import ru.practicum.ewm.dto.ParamDto;
import ru.practicum.ewm.dto.StatDto;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.dto.event.AdminSearchEventDto;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.PrivateSearchEventDto;
import ru.practicum.ewm.dto.event.PublicSearchEventParams;
import ru.practicum.ewm.dto.event.StatEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.model.ActionState;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Sorting;
import ru.practicum.ewm.RequestServiceFeignClient;
import ru.practicum.ewm.UserServiceFeignClient;
import ru.practicum.ewm.dto.EventState;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.exception.ConflictDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.QEvent;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.dto.ParamEventDto;
import ru.practicum.ewm.dto.RequestCountDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserServiceFeignClient userClient;
    private final CategoryRepository categoryRepository;
    private final RequestServiceFeignClient requestClient;
    private final EventMapper eventMapper;
    private final StatClient statClient;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Override
    public Collection<EventShortDto> findBy(PrivateSearchEventDto params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        List<Event> events = eventRepository.findAllByInitiatorId(params.getUserId(), pageable);
        return mapToShortDto(events);
    }

    @Override
    @Transactional
    public EventFullDto create(long userId, NewEventDto newEvent) {
        UserShortDto user = getUser(userId);
        Category category = getCategory(newEvent.getCategory());
        Event event = eventMapper.map(newEvent);
        event.setCategory(category);
        event.setInitiatorId(user.getId());
        event.setInitiatorName(user.getName());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        eventRepository.save(event);
        log.info("Event save {}", event);
        return eventMapper.mapToFullDto(event, null, null);
    }

    @Override
    public EventFullDto findBy(ParamEventDto paramEventDto, String ip) {
        Event event = getUserEvent(paramEventDto);
        Map<Long, Long> countConfirmedRequest = getCountConfirmedRequest(List.of(event));
        Map<Long, Long> stat = getStat(List.of(event));
        addHit(createEventUri(event), ip);
        return eventMapper.mapToFullDto(event, stat.get(event.getId()), countConfirmedRequest.get(event.getId()));
    }

    @Override
    @Transactional
    public EventFullDto update(ParamEventDto paramEventDto, UpdateEventUserRequest updateEvent) {
        Event event = getUserEvent(paramEventDto);
        checkPublished(event);
        updateEventsStatus(event, updateEvent);
        Category category = checkCategory(updateEvent.getCategory());
        eventMapper.update(event, updateEvent, category);
        eventRepository.save(event);
        Map<Long, Long> countConfirmedRequest = getCountConfirmedRequest(List.of(event));
        Map<Long, Long> stat = getStat(List.of(event));
        return eventMapper.mapToFullDto(event, stat.get(event.getId()), countConfirmedRequest.get(event.getId()));
    }

    @Override
    public Collection<EventFullDto> findEventsAdmin(AdminSearchEventDto params) {
        Predicate query = buildQueryAdmin(params);
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        List<Event> events = eventRepository.findAll(query, pageable).getContent();
        return mapToFullDto(events);
    }


    @Override
    public EventFullDto findEventByIdPublic(long id, String ip) {
        Event event = getEvent(id);
        if (event.getState() != EventState.PUBLISHED) {
            log.error("Event with ID = {} is not published", id);
            throw new NotFoundException("Event not found");
        }
        Map<Long, Long> countConfirmedRequest = getCountConfirmedRequest(List.of(event));
        Map<Long, Long> stat = getStat(List.of(event));
        addHit(createEventUri(event), ip);
        return eventMapper.mapToFullDto(event, stat.get(event.getId()), countConfirmedRequest.get(event.getId()));
    }

    @Override
    public Collection<EventShortDto> findEventsPublic(PublicSearchEventParams params) {
        Predicate predicate = buildQueryPublic(params);
        Sort sort = getSortingValue(params.getSort());
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize(), sort);
        List<Event> events = eventRepository.findAll(predicate, pageable).getContent();
        List<EventShortDto> eventShortDtoList = mapToShortDto(events);
        addHit("/events", params.getIp());
        return eventShortDtoList;
    }

    @Override
    @Transactional
    public EventFullDto update(long eventId, UpdateEventUserRequest updateEvent) {
        Event event = getEvent(eventId);
        checkEventDate(event);
        updateEventsStatus(event, updateEvent);
        Category category = checkCategory(updateEvent.getCategory());
        eventMapper.update(event, updateEvent, category);
        eventRepository.save(event);
        log.info("Event updated {}", event);
        Map<Long, Long> countConfirmedRequest = getCountConfirmedRequest(List.of(event));
        Map<Long, Long> stat = getStat(List.of(event));
        return eventMapper.mapToFullDto(event, stat.get(event.getId()), countConfirmedRequest.get(event.getId()));
    }

    @Override
    public EventWithInitiatorDto findBy(long eventId) {
        Event event = getEvent(eventId);
        return eventMapper.mapToInitiatorDto(event);
    }

    private Category getCategory(long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Not found category with ID = {}", categoryId);
                    return new NotFoundException(String.format("Not found category ID = %d", categoryId));
                });
    }

    private Category checkCategory(Optional<Long> categoryId) {
        if (categoryId != null) {
            return getCategory(categoryId.get());
        } else {
            return null;
        }
    }

    private UserShortDto getUser(long userId) {
        try {
            return userClient.findShortUsers(List.of(userId)).getFirst();
        } catch (FeignException e) {
            log.error("Not found user with ID = {}", userId);
            throw new NotFoundException(String.format("Not found user with ID = %d", userId));
        }
    }

    private Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Not found event with ID = {}", eventId);
                    return new NotFoundException(String.format("Not found event with ID = %d", eventId));
                });
    }

    private Event getUserEvent(ParamEventDto paramEventDto) {
        long userId = paramEventDto.getUserId();
        long eventId = paramEventDto.getEventId();
        Event event = getEvent(eventId);
        if (event.getInitiatorId() != userId) {
            log.error("Event with ID = {} is not found", eventId);
            throw new NotFoundException(
                    String.format("Not found event with ID = %d", eventId));
        }
        return event;
    }

    private Map<Long, Long> getCountConfirmedRequest(List<Event> events) {
        List<Long> ids = events.stream().map(Event::getId).toList();
        try {
            return requestClient.findConfirmedRequest(ids).stream()
                    .collect(Collectors.toMap(RequestCountDto::getEventId, RequestCountDto::getCount));
        } catch (RuntimeException e) {
            return new HashMap<>();
        }
    }

    private Map<Long, Long> getStat(List<Event> events) {
        List<String> uris = events.stream().map(this::createEventUri).toList();
        String start = events.stream().map(Event::getCreatedOn).sorted().findFirst().get().format(dateTimeFormatter);
        String end = LocalDateTime.now().format(dateTimeFormatter);
        ParamDto statParam = new ParamDto(start, end, uris, true);
        List<StatDto> statDto = statClient.stat(statParam);
        return statDto.stream().map(dto -> new StatEventDto(parseUri(dto.getUri()), dto.getHits()))
                .collect(Collectors.toMap(StatEventDto::getEventId, StatEventDto::getHits));
    }

    private void addHit(String uri, String ip) {
        HitDto hitDto = new HitDto(0,
                "ewm-main-service",
                uri,
                ip,
                LocalDateTime.now().format(dateTimeFormatter));
        statClient.hit(hitDto);
    }

    private int parseUri(String uri) {
        String[] split = uri.split("/");
        return Integer.parseInt(split[2]);
    }

    private String createEventUri(Event event) {
        return String.format("/events/%d", event.getId());
    }

    private void checkEventDate(Event event) {
        LocalDateTime eventDate = event.getEventDate();
        if (eventDate.minusHours(1).isBefore(LocalDateTime.now())) {
            log.error("Event ID = {} is not available for change now", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d is not available for change now", event.getId()));
        }
    }

    private void checkEventStatePending(Event event) {
        if (!event.getState().equals(EventState.PENDING)) {
            log.error("Event ID = {} not in the status for review", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d not in the status for review", event.getId()));
        }
    }

    private void checkPublished(Event event) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            log.error("Event ID = {} not in the correct status for review", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d not in the status for review", event.getId()));
        }
    }


    private void updateEventsStatus(Event event, UpdateEventUserRequest updateEvent) {
        ActionState actionState;
        if (updateEvent.getStateAction() != null) {
            actionState = updateEvent.getStateAction();
            switch (actionState) {
                case PUBLISH_EVENT -> {
                    checkEventStatePending(event);
                    event.setPublishedOn(LocalDateTime.now());
                    event.setState(EventState.PUBLISHED);
                }
                case REJECT_EVENT -> {
                    checkPublished(event);
                    event.setState(EventState.CANCELED);
                }
                case CANCEL_REVIEW -> event.setState(EventState.CANCELED);
                case SEND_TO_REVIEW -> event.setState(EventState.PENDING);
            }
        }
    }

    private Predicate buildQueryAdmin(AdminSearchEventDto params) {
        BooleanBuilder searchParams = new BooleanBuilder();

        if (params.getUsers() != null && !params.getUsers().isEmpty()) {
            searchParams.and(QEvent.event.initiatorId.in(params.getUsers()));
        }
        if (params.getStates() != null && !params.getStates().isEmpty()) {
            searchParams.and(QEvent.event.state.in(params.getStates()));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            searchParams.and(QEvent.event.category.id.in(params.getCategories()));
        }
        if (params.getRangeStart() != null) {
            searchParams.and(QEvent.event.eventDate.after(params.getRangeStart()));
        }
        if (params.getRangeEnd() != null) {
            searchParams.and(QEvent.event.eventDate.before(params.getRangeEnd()));
        }

        return searchParams;
    }

    private Predicate buildQueryPublic(PublicSearchEventParams params) {
        BooleanBuilder searchParams = new BooleanBuilder();

        if (params.getText() != null && !params.getText().isBlank()) {
            searchParams.and(QEvent.event.description.contains(params.getText())
                    .or(QEvent.event.annotation.contains(params.getText())));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            searchParams.and(QEvent.event.category.id.in(params.getCategories()));
        }
        if (params.getPaid() != null) {
            searchParams.and(QEvent.event.paid.eq(params.getPaid()));
        }
        if (params.getRangeStart() != null) {
            searchParams.and(QEvent.event.eventDate.after(params.getRangeStart()));
        }
        if (params.getRangeEnd() != null) {
            searchParams.and(QEvent.event.eventDate.before(params.getRangeEnd()));
        }

        return searchParams;
    }

    private Sort getSortingValue(Sorting sortParam) {
        return switch (sortParam) {
            case EVENT_DATE -> Sort.by(Sort.Direction.DESC, "eventDate");
            case VIEWS -> Sort.by(Sort.Direction.DESC, "views");
            case null -> Sort.unsorted();
        };

    }

    private List<EventShortDto> mapToShortDto(List<Event> events) {
        Map<Long, Long> countConfirmedRequest = getCountConfirmedRequest(events);
        Map<Long, Long> stat = getStat(events);
        return events.stream()
                .map(event -> {
                    Long confirmedCount = countConfirmedRequest.get(event.getId());
                    Long statValue = stat.get(event.getId());
                    return eventMapper.mapToShortDto(event, statValue, confirmedCount);
                })
                .toList();
    }

    private List<EventFullDto> mapToFullDto(List<Event> events) {
        Map<Long, Long> countConfirmedRequest = getCountConfirmedRequest(events);
        Map<Long, Long> stat = getStat(events);
        return events.stream()
                .map(event -> {
                    Long confirmedCount = countConfirmedRequest.get(event.getId());
                    Long statValue = stat.get(event.getId());
                    return eventMapper.mapToFullDto(event, statValue, confirmedCount);
                })
                .toList();
    }
}

