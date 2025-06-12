package ru.practicum.event.service;

import com.google.protobuf.Timestamp;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.QEvent;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictStateException;
import ru.practicum.exception.ConflictTimeException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.grpc.stats.*;
import ru.practicum.request.client.RequestClient;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.UserShortDto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationService locationService;
    private final CategoryService categoryService;
    private final ViewService viewService;
    private final EventMapper mp;
    private final LocationMapper lmp;
    private final QEvent event = QEvent.event;
    private final RequestClient requestClient;
    private final UserClient userClient;
    private final UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;
    private final AnalyzerControllerGrpc.AnalyzerControllerBlockingStub analyzerClient;

    @Transactional
    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        Event ev = mp.toEntity(newEventDto);
        ev.setCategory(categoryService.getCategory(ev.getCategory().getId()));
        ev.setInitiator(userId);
        log.info("Создание события {}", ev);
        Event savedEvent = eventRepository.save(ev);
        return addUserShortDtoToFullDto(savedEvent, userId);
    }

    @Override
    public List<EventShortDto> getEventsForUser(EventDtoGetParam prm) {
        log.info("Получение списка мероприятий для пользователя с id {} ", prm.getUserId());
        Predicate predicate = event.initiator.eq(prm.getUserId());
        PageRequest pageRequest = PageRequest.of(prm.getFrom(), prm.getSize());
        List<Event> events = eventRepository.findAll(predicate, pageRequest).getContent();

        if (events.isEmpty()) {
            return new ArrayList<>();
        }
        List<EventShortDto> dtos = new ArrayList<>();
        for (Event ev : events) {
            dtos.add(addUserShortDtoToShortDto(ev, ev.getInitiator()));
        }

        return dtos;
    }

    @Override
    public EventFullDto getEventByIdForUser(EventDtoGetParam prm) {
        Predicate predicate = event.initiator.eq(prm.getUserId())
                .and(event.id.eq(prm.getEventId()));
        Event ev = eventRepository.findOne(predicate)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d для пользователя с id %d не найдено.",
                                prm.getEventId(), prm.getUserId())));

        log.info("Получение события с id {}  для пользователя с id {}", prm.getEventId(), prm.getUserId());
        return addUserShortDtoToFullDto(ev, prm.getUserId());
    }

    @Override
    public List<EventFullDto> getEventsForAdmin(EventDtoGetParam prm) {
        Predicate predicate = null;
        if (prm.getUsers() != null && !prm.getUsers().isEmpty()) {
            predicate = ExpressionUtils.and(null, event.initiator.in(prm.getUsers()));
        }
        if (prm.getStates() != null && !prm.getStates().isEmpty()) {
            List<State> states = prm.getStates().stream()
                    .map(State::valueOf) // Преобразуем строки в перечисление
                    .toList();
            predicate = ExpressionUtils.and(predicate, event.state.in(states));
        }
        if (prm.getCategories() != null && !prm.getCategories().isEmpty()) {
            predicate = ExpressionUtils.and(predicate, event.category.id.in(prm.getCategories()));
        }
        if (prm.getRangeStart() != null && prm.getRangeEnd() != null) {
            dateValid(prm.getRangeStart(), prm.getRangeEnd());
            predicate = ExpressionUtils.and(predicate, event.eventDate.between(prm.getRangeStart(), prm.getRangeEnd()));
        }
        PageRequest pageRequest = PageRequest.of(prm.getFrom(), prm.getSize());
        List<Event> events;
        events = (predicate == null)
                ? eventRepository.findAll(pageRequest).getContent()
                : eventRepository.findAll(predicate, pageRequest).getContent();
        log.info("Получение списка событий администратором с параметрами {} и предикатом {}", prm, predicate);
        List<EventFullDto> dtos = new ArrayList<>();
        for (Event ev : events) {
            dtos.add(addUserShortDtoToFullDto(ev, ev.getInitiator()));
        }
        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long id, UpdateEventAdminRequest rq) {
        Event ev = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d не найдено.", id)));
        if ((rq.getStateAction() == StateAction.PUBLISH_EVENT && ev.getState() != State.PENDING) ||
                (rq.getStateAction() == StateAction.REJECT_EVENT && ev.getState() == State.PUBLISHED)) {
            throw new ConflictStateException(
                    (rq.getStateAction() == StateAction.PUBLISH_EVENT) ?
                            "Невозможно опубликовать событие, так как текущий статус не PENDING"
                            : "Нельзя отменить публикацию, так как событие уже опубликовано");
        }
        ev.setState(State.CANCELED);
        if (rq.getLocation() != null) {
            Location sLk = locationService.getLocation(lmp.toLocation(rq.getLocation()));
            ev.setLocation(sLk);
        }
        mp.updateFromAdmin(rq, ev);
        ev.setState(rq.getStateAction() == StateAction.PUBLISH_EVENT ? State.PUBLISHED : State.CANCELED);
        if (rq.getStateAction() == StateAction.PUBLISH_EVENT) {
            ev.setPublishedOn(LocalDateTime.now());
        }
        log.info("Обновление события с id {} администратором с параметрами {}", id, rq);
        Event savedEvent = eventRepository.save(ev);
        return addUserShortDtoToFullDto(savedEvent, savedEvent.getInitiator());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest rq) {
        if (rq.getEventDate() != null && rq.getEventDate().isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new ConflictTimeException("Время не может быть раньше, через два часа от текущего момента");
        }
        Event ev = findByIdAndInitiator(eventId, userId);
        if (ev.getState() == State.PUBLISHED) {
            throw new ConflictStateException("Изменить можно только неопубликованное событие");
        }
        if (rq.getStateAction() != null) {
            switch (rq.getStateAction()) {
                case SEND_TO_REVIEW -> ev.setState(State.PENDING);
                case CANCEL_REVIEW -> ev.setState(State.CANCELED);
                default -> throw new IllegalArgumentException("Неизвестный статус: " + rq.getStateAction());
            }
        }
        if (rq.getLocation() != null) {
            ev.setLocation(locationService.getLocation(lmp.toLocation(rq.getLocation())));
        }
        mp.updateFromUser(rq, ev);
        Event savedEvent = eventRepository.save(ev);
        return addUserShortDtoToFullDto(savedEvent, savedEvent.getInitiator());
    }

    @Override
    @Transactional
    public List<EventShortDto> getPublicEvents(EventDtoGetParam prm, HttpServletRequest rqt) {
        Predicate predicate = event.state.eq(State.PUBLISHED);
        if (prm.getText() != null && !prm.getText().isEmpty()) {
            predicate = ExpressionUtils.and(predicate, (event.annotation.containsIgnoreCase(prm.getText())).or(
                    event.description.containsIgnoreCase(prm.getText())));
        }
        if (prm.getCategories() != null && !prm.getCategories().isEmpty()) {
            predicate = ExpressionUtils.and(predicate, event.category.id.in(prm.getCategories()));
        }
        if (prm.getPaid() != null) {
            predicate = ExpressionUtils.and(predicate, event.paid.eq(prm.getPaid()));
        }
        if (prm.getRangeStart() != null && prm.getRangeEnd() != null) {
            dateValid(prm.getRangeStart(), prm.getRangeEnd());
            predicate = ExpressionUtils.and(predicate, event.eventDate.between(prm.getRangeStart(), prm.getRangeEnd()));
        } else {
            predicate = ExpressionUtils.and(predicate, event.eventDate.gt(LocalDateTime.now())); // проверить
        }
        if (prm.getOnlyAvailable() != null && prm.getOnlyAvailable()) { //проверка есть ли еще места на мероприятие
            predicate = ExpressionUtils.and(predicate, (event.participantLimit.eq(0)).or(
                    event.participantLimit.subtract(requestClient.getAllByEventId(prm.getUserId(), prm.getEventId())
                            .size()).gt(0)));
        }
        Sort sort = Sort.unsorted();
        if (prm.getSort() != null) {
            if (prm.getSort().equals("EVENT_DATE")) {
                sort = Sort.by(Sort.Direction.ASC, "eventDate");
            } else if (prm.getSort().equals("VIEWS")) {
                sort = Sort.by(Sort.Direction.DESC, "views");
            }
        }
        PageRequest pageRequest = PageRequest.of(prm.getFrom(), prm.getSize(), sort);
        List<Event> events = eventRepository.findAll(predicate, pageRequest).getContent();
        if (!events.isEmpty()) {
            viewService.saveViews(events, rqt);
        }
        List<EventShortDto> dtos = new ArrayList<>();
        for (Event ev : events) {
            dtos.add(addUserShortDtoToShortDto(ev, ev.getInitiator()));
        }
        return dtos;
    }

    @Override
    @Transactional
    public EventFullDto getPublicEventById(Long id, Long userId, HttpServletRequest rqt) {
        Predicate predicate = event.state.eq(State.PUBLISHED).and(event.id.eq(id));
        Event ev = eventRepository.findOne(predicate)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d не найдено.", id)));
        viewService.saveView(ev, rqt);

        sendToCollector(userId, id, ActionTypeProto.ACTION_VIEW);

        return addUserShortDtoToFullDto(ev, ev.getInitiator());
    }

    @Override
    public List<Event> getAllEventByIds(List<Long> ids) {
        return eventRepository.findAllById(ids);
    }

    @Override
    public EventFullDto getEventById(Long id) {
        return mp.toEventFullDto(eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдено события с id: " + id)));
    }

    @Override
    public List<ParticipationRequestDto> getAllByInitiatorIdAndEventId(long userId, long eventId) {
        findByIdAndInitiator(eventId, userId);
        return requestClient.getAllByEventId(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(EventRequestStatusUpdateRequest updateRequest,
                                                                               long userId, long eventId) {
        log.info("Смена статусов запросов для события: {}, пользователем: {}", eventId, userId);
        EventFullDto eventFullDto = addUserShortDtoToFullDto(findByIdAndInitiator(eventId, userId), userId);
        RequestStatusUpdateDto dto = RequestStatusUpdateDto.builder()
                .updateRequest(updateRequest)
                .event(eventFullDto)
                .build();
        return requestClient.updateRequestStatus(userId, dto);
    }

    @Override
    public void likeEvent(Long userId, Long eventId) {
        log.info("Пользователь: {}, лайкнул событие: {}", userId, eventId);

        Long ownerId = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Не найдено мероприятие с id: " + eventId)).getId();

        if (requestClient.getAllByEventId(ownerId, eventId).stream()
                .noneMatch(request -> request.getRequester().equals(userId))) {
            throw new ValidationException(String.format("Пользователь: {}, не учавствовал в мероприятии: {}",
                    userId, eventId));
        }

        sendToCollector(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

    @Override
    public List<EventShortDto> findRecommendation(Long userId) {

        RecommendedEventProto proto = analyzerClient.getRecommendationsForUser(UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .build());

        return List.of();
    }

    private void dateValid(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала события позже даты окончания");
        }
    }

    private Event findByIdAndInitiator(Long eventId, Long initiatorId) {
        return eventRepository.findByIdAndInitiator(eventId, initiatorId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Событие с id %d для пользователя с id %d не найдено.", eventId, initiatorId)));
    }

    private EventFullDto addUserShortDtoToFullDto(Event event, Long userId) {
        EventFullDto dto = mp.toEventFullDto(event);
        UserShortDto userDto = userClient.getUser(userId).getBody();
        dto.setInitiator(userDto);
        dto.setConfirmedRequests(requestClient.getAllByEventId(userId, event.getId()).size());
        return dto;
    }

    private EventShortDto addUserShortDtoToShortDto(Event event, Long userId) {
        EventShortDto dto = mp.toEventShortDto(event);
        UserShortDto userDto = userClient.getUser(userId).getBody();
        dto.setInitiator(userDto);
        return dto;
    }

    private void sendToCollector(Long userId, Long eventId, ActionTypeProto actionType) {
        Instant instant = Instant.now();
        collectorClient.collectUserAction(UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build())
                .build());
    }
}

