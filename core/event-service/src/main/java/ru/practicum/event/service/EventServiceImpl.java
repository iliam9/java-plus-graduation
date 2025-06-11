package ru.practicum.event.service;

import com.querydsl.core.types.Predicate;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.*;
import ru.practicum.comments.mapper.CommentMapper;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.events.CommentDto;
import ru.practicum.dto.enums.CommentStatus;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.enums.RequestStatus;
import ru.practicum.dto.events.EventDto;
import ru.practicum.dto.events.LocationDto;
import ru.practicum.dto.requests.ParticipationRequestDto;
import ru.practicum.dto.users.UserDto;
import ru.practicum.dto.users.UserShortDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.model.Location;
import ru.practicum.event.predicates.EventPredicates;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.OperationForbiddenException;
import ru.practicum.grpc.messages.RecommendedEventProto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryClient categoryClient;
    private final UserClient userClient;
    private final RequestClient requestClient;
    private final CollectorClient collectorClient;
    private final RecommendationClient recommendationClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public List<EventDto> adminEventsSearch(SearchEventsParam param) {
        Pageable pageable = PageRequest.of(param.getFrom(), param.getSize());
        Predicate predicate = EventPredicates.adminFilter(param);
        List<Event> events;
        if (predicate == null) {
             events = eventRepository.findAll(pageable).stream().toList();
        } else {
            events = eventRepository.findAll(predicate, pageable).stream().toList();
        }

        List<EventDto> eventDtoList = new ArrayList<>();
        for (Event event : events) {
            EventDto eventDto = parseCategoryAndInitiator(event);
            eventDtoList.add(eventDto);
        }
        return addMinimalDataToList(eventDtoList);
    }

    @Override
    public EventDto adminEventUpdate(Long eventId, EventAdminUpdateDto eventUpdateDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        if (eventUpdateDto.getEventDate() != null && eventUpdateDto.getEventDate().isBefore(event.getCreatedOn().minusHours(1))) {
            throw new ValidationException("Event date cannot be before created date");
        }

        updateEventData(event, eventUpdateDto.getTitle(),
                eventUpdateDto.getAnnotation(),
                eventUpdateDto.getDescription(),
                eventUpdateDto.getCategoryId(),
                eventUpdateDto.getEventDate(),
                eventUpdateDto.getLocation(),
                eventUpdateDto.getPaid(),
                eventUpdateDto.getRequestModeration(),
                eventUpdateDto.getParticipantLimit());
        if (eventUpdateDto.getStateAction() != null) {
            if (!event.getState().equals(EventState.PENDING)) {
                throw new OperationForbiddenException("Can't reject not pending event");
            }
            if (eventUpdateDto.getStateAction().equals(AdminUpdateStateAction.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
            }
            if (eventUpdateDto.getStateAction().equals(AdminUpdateStateAction.REJECT_EVENT)) {
                event.setState(EventState.CANCELED);
            }
        }
        event = eventRepository.save(event);
        EventDto eventDto = parseCategoryAndInitiator(event);
        return addAdvancedData(eventDto);
    }

    @Override
    public List<EventShortDto> getEvents(EntityParam params) {
        LocalDateTime rangeStart = params.getRangeStart();
        LocalDateTime rangeEnd = params.getRangeEnd();
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new ValidationException("Start date can not be after end date");
            }
        }

        Predicate predicate = EventPredicates.publicFilter(params.getText(), params.getCategories(), rangeStart,
                rangeEnd, params.getPaid());
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());

        List<Event> filteredEvents;
        if (predicate != null) {
            filteredEvents = eventRepository.findAll(predicate, pageable).toList();
        } else {
            filteredEvents = eventRepository.findAll(pageable).toList();
        }

        if (params.getOnlyAvailable()) {
            filteredEvents = filteredEvents.stream().filter(this::isEventAvailable).toList();
        }

        List<EventShortDto> eventDtos = new ArrayList<>();
        for (Event event : filteredEvents) {
            EventShortDto eventDto = eventMapper.toEventShortDto(event);
            CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
            eventDto.setCategory(category);
            UserDto initiator = userClient.getUsers(List.of(event.getInitiatorId()), 0, 1).getFirst();
            UserShortDto initiatorShortInfo = new UserShortDto(initiator.getId(), initiator.getName());
            eventDto.setInitiator(initiatorShortInfo);
            eventDtos.add(eventDto);
        }

        EventSort sort = params.getSort();
        if (sort != null) {
            switch (sort) {
                case EVENT_DATE ->
                        eventDtos.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).toList();
                case RATING -> eventDtos.stream().sorted(Comparator.comparing(EventShortDto::getRating)).toList();
            }
        }
        return eventDtos;
    }

    @Override
    public EventDto getEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));

        EventDto eventDto = parseCategoryAndInitiator(event);
        collectorClient.sendView(userId, eventId);
        return addAdvancedData(eventDto);
    }


    private void updateEventData(Event event, String title, String annotation, String description, Long categoryId, LocalDateTime eventDate, LocationDto location, Boolean paid, Boolean requestModeration, Integer participantLimit) {
        if (title != null) {
            event.setTitle(title);
        }
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (categoryId != null) {
            CategoryDto category = categoryClient.getCategoryById(categoryId);
            if (category == null) {
                throw new NotFoundException("Category not found");
            }

            event.setCategoryId(category.getId());
        }
        if (eventDate != null) {
            if (eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new ValidationException(String.format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s", eventDate));
            }
            event.setEventDate(eventDate);
        }
        if (location != null) {
            Location newLocation = locationRepository.save(locationMapper.toLocation(location));
            event.setLocation(newLocation);
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
    }

    @Override
    public List<EventDto> privateUserEvents(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);
        List<EventDto> list = eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(eventMapper::toDto)
                .toList();
        return addAdvancedDataToList(list);
    }

    @Override
    public EventDto privateEventCreate(Long userId, EventCreateDto eventCreateDto) {
        if (eventCreateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException(String
                    .format("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: %s",
                            eventCreateDto.getEventDate()));
        }
        UserDto initiator = userClient.getUsers(List.of(userId), 0, 1).getFirst();
        if (initiator == null) {
            throw  new NotFoundException("User not found");
        }

        Event event = eventMapper.fromDto(eventCreateDto);
        event.setInitiatorId(initiator.getId());
        CategoryDto category = categoryClient.getCategoryById(eventCreateDto.getCategory());
        if (category == null) {
            throw new NotFoundException("Category not found");
        }

        event.setCategoryId(eventCreateDto.getCategory());
        locationRepository.save(event.getLocation());
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        event = eventRepository.save(event);

        EventDto eventDto = eventMapper.toDto(event);
        eventDto.setCategory(category);
        UserShortDto initiatorShortInfo = new UserShortDto(initiator.getId(), initiator.getName());
        eventDto.setInitiator(initiatorShortInfo);
        return addAdvancedData(eventDto);
    }

    @Override
    public EventDto privateGetUserEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));
        return addAdvancedData(eventMapper.toDto(event));
    }

    @Override
    public EventDto privateUpdateUserEvent(Long userId, Long eventId, EventUpdateDto eventUpdateDto) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with id %s not found", eventId)));
        if (event.getState().equals(EventState.PUBLISHED) || event.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new OperationForbiddenException("Only pending or canceled events can be changed");
        }
        updateEventData(event, eventUpdateDto.getTitle(),
                eventUpdateDto.getAnnotation(),
                eventUpdateDto.getDescription(),
                eventUpdateDto.getCategoryId(),
                eventUpdateDto.getEventDate(),
                eventUpdateDto.getLocation(),
                eventUpdateDto.getPaid(),
                eventUpdateDto.getRequestModeration(),
                eventUpdateDto.getParticipantLimit());
        if (eventUpdateDto.getStateAction() != null) {
            if (eventUpdateDto.getStateAction().equals(UpdateStateAction.SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            }
            if (eventUpdateDto.getStateAction().equals(UpdateStateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }
        event = eventRepository.save(event);
        EventDto eventDto = parseCategoryAndInitiator(event);
        return addAdvancedData(eventDto);
    }

    @Override
    public List<EventDto> getEvents(List<Long> ids) {
        List<EventDto> result = new ArrayList<>();
        if (ids == null || ids.isEmpty()) {
            result = eventRepository.findAll().stream().map(eventMapper::toDto).toList();
        } else {
            result = eventRepository.findAllByIdIn(ids).stream().map(eventMapper::toDto).toList();
        }
        for (EventDto event : result) {
            event.setConfirmedRequests(requestClient.countRequestsByEventIdAndStatus(0L, event.getId(), RequestStatus.CONFIRMED));
        }
        return result;
    }

    @Override
    public List<EventDto> getRecommendedEventsForUser(Long userId) {
        List<Long> eventIds = recommendationClient.getRecommendationsForUser(userId, 10)
                .map(RecommendedEventProto::getEventId)
                .toList();
        return eventRepository.findAllById(eventIds).stream().map(eventMapper::toDto).toList();
    }

    @Override
    public void addLikeToEvent(Long userId, Long eventId) {
        if (requestClient.findByRequesterIdAndEventIdAndStatus(userId, eventId, RequestStatus.CONFIRMED) != null
                && eventRepository.findById(eventId).get().getEventDate().isBefore(LocalDateTime.now())) {
            collectorClient.sendLike(userId, eventId);

        } else {
            throw new ValidationException("Пользователь с id: " + userId + " не имеет права лайкать мероприятие с id: "
            + eventId + " т.к. не посещал его");
        }
    }

    @Override
    public List<EventDto> findAllByCategoryId(Long categoryId) {
        return eventRepository.findAllByCategoryId(categoryId).stream().map(eventMapper::toDto).toList();
    }

    @Override
    public EventDto findByIdAndInitiatorId(Long eventId, Long userId) {
        Optional<EventDto> result = eventRepository.findByIdAndInitiatorId(eventId, userId).map(eventMapper::toDto);
        return result.orElse(null);
    }

    @Override
    public List<EventDto> findAllByInitiatorId(Long initiatorId) {
        return eventRepository.findAllByInitiatorId(initiatorId).stream().map(eventMapper::toDto).toList();
    }


    private EventDto addAdvancedData(EventDto eventDto) {
        Double rating = recommendationClient.getInteractionsCount(List.of(eventDto.getId()))
                .map(RecommendedEventProto::getScore)
                .reduce(0D, Double::sum);
        eventDto.setRating(rating);

        eventDto.setConfirmedRequests(requestClient.countRequestsByEventIdAndStatus(0L, eventDto.getId(), RequestStatus.CONFIRMED));

        List<CommentDto> comments = commentRepository.findByEventIdAndStatus(eventDto.getId(), CommentStatus.PUBLISHED).stream()
                .map(commentMapper::toDto).toList();
        eventDto.setComments(comments);

        return eventDto;
    }

    private boolean isEventAvailable(Event event) {
        Long confirmedRequestsAmount = requestClient.countRequestsByEventIdAndStatus(0L, event.getId(), RequestStatus.CONFIRMED);
        return event.getParticipantLimit() > confirmedRequestsAmount;
    }

    private HashMap<Long, Long> getEventConfirmedRequestsCount(List<Long> idsList) {
        List<ParticipationRequestDto> requests = requestClient.findAllByEventIdInAndStatus(idsList, 0L,RequestStatus.CONFIRMED);
        HashMap<Long, Long> confirmedRequestMap = new HashMap<>();
        for (ParticipationRequestDto request : requests) {
            if (confirmedRequestMap.containsKey(request.getEvent())) {
                confirmedRequestMap.put(request.getEvent(), confirmedRequestMap.get(request.getEvent()) + 1);
            } else {
                confirmedRequestMap.put(request.getEvent(), 1L);
            }
        }
        for (Long id : idsList) {
            if (!confirmedRequestMap.containsKey(id)) {
                confirmedRequestMap.put(id, 0L);
            }
        }
        return confirmedRequestMap;
    }

    private HashMap<Long, List<CommentDto>> getEventComments(List<Long> idsList) {
        List<CommentDto> comments = commentRepository.findAllByEventIdInAndStatus(idsList, CommentStatus.PUBLISHED).stream()
                .map(commentMapper::toDto).toList();
        HashMap<Long, List<CommentDto>> commentsMap = new HashMap<>();
        for (CommentDto comment : comments) {
            if (!commentsMap.containsKey(comment.getEventId())) {
                commentsMap.put(comment.getEventId(), new ArrayList<>());
            }
            commentsMap.get(comment.getEventId()).add(comment);
        }
        return commentsMap;
    }

    private HashMap<Long, Double> getEventRating(List<Long> idsList) {
        List<RecommendedEventProto> ratings = recommendationClient.getInteractionsCount(idsList).collect(Collectors.toList());
        HashMap<Long, Double> ratingMap = new HashMap<>();
        for (RecommendedEventProto recommendedEvent : ratings) {
            ratingMap.put(recommendedEvent.getEventId(), recommendedEvent.getScore());
        }
        return ratingMap;
    }

    private List<EventDto> addMinimalDataToList(List<EventDto> eventDtoList) {

        List<Long> idsList = eventDtoList.stream().map(EventDto::getId).toList();
        HashMap<Long, Long> confirmedMap = getEventConfirmedRequestsCount(idsList);

        return eventDtoList.stream()
                .peek(dto -> dto.setConfirmedRequests(confirmedMap.get(dto.getId())))
                .toList();
    }

    private List<EventDto> addAdvancedDataToList(List<EventDto> eventDtoList) {

        List<Long> idsList = eventDtoList.stream().map(EventDto::getId).toList();
        HashMap<Long, Double> ratingMap = getEventRating(idsList);
        HashMap<Long, Long> confirmedMap = getEventConfirmedRequestsCount(idsList);
        HashMap<Long, List<CommentDto>> commentMap = getEventComments(idsList);

        return eventDtoList.stream()
                .peek(dto -> dto.setComments(commentMap.get(dto.getId())))
                .peek(dto -> dto.setRating(ratingMap.get(dto.getId())))
                .peek(dto -> dto.setConfirmedRequests(confirmedMap.get(dto.getId())))
                .toList();
    }

    private List<EventShortDto> addAdvancedDataToShortDtoList(List<EventShortDto> eventShortDtoList) {

        List<Long> idsList = eventShortDtoList.stream().map(EventShortDto::getId).toList();
        HashMap<Long, Double> ratingMap = getEventRating(idsList);
        HashMap<Long, Long> confirmedMap = getEventConfirmedRequestsCount(idsList);

        return eventShortDtoList.stream()
                .peek(dto -> dto.setRating(ratingMap.get(dto.getId())))
                .peek(dto -> dto.setConfirmedRequests(confirmedMap.get(dto.getId())))
                .toList();
    }

    private EventDto parseCategoryAndInitiator(Event event) {
        EventDto eventDto = eventMapper.toDto(event);
        CategoryDto category = categoryClient.getCategoryById(event.getCategoryId());
        eventDto.setCategory(category);
        UserDto initiator = userClient.getUsers(List.of(event.getInitiatorId()), 0, 1).getFirst();
        UserShortDto initiatorShortInfo = new UserShortDto(initiator.getId(), initiator.getName());
        eventDto.setInitiator(initiatorShortInfo);
        return eventDto;
    }

}
