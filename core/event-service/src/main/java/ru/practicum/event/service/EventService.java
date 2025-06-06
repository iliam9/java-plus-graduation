package ru.practicum.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.client.UserClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;

import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private static final long HOURS_BEFORE_EVENT = 2;

    private final EventRepository eventRepository;
    private final UserClient userClient;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    public List<EventShortDto> getAllEventsOfUser(Long userId, int from, int size) {
        validatePaginationParams(from, size);
        getUserOrThrow(userId);

        PageRequest pageRequest = PageRequest.of(from / size, size);
        Page<Event> eventPage = eventRepository.findAllByInitiatorId(userId, pageRequest);

        return eventMapper.toEventShortDto(eventPage.getContent());
    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto dto) {
        User initiator = getUserOrThrow(userId);
        Category category = getCategoryOrThrow(dto.getCategory());

        validateEventDate(dto.getEventDate());

        Event event = eventMapper.toEvent(dto, initiator, category);
        Event saved = eventRepository.save(event);
        return eventMapper.toEventFullDto(saved);
    }

    public EventFullDto getEventOfUser(Long userId, Long eventId) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event does not belong to user id=" + userId);
        }

        return eventMapper.toEventFullDto(event);
    }

    @Transactional
    public EventFullDto updateEventOfUser(Long userId, Long eventId, UpdateEventUserRequest dto) {
        getUserOrThrow(userId);
        Event event = getEventOrThrow(eventId);

        if (!event.getInitiatorId().equals(userId)) {
            throw new NotFoundException("Event does not belong to user id=" + userId);
        }

        if (EventState.PUBLISHED.equals(event.getState())) {
            throw new ConflictException("Cannot modify already published event");
        }

        if (dto.getEventDate() != null) {
            validateEventDate(dto.getEventDate());
        }

        Category category = null;
        if (dto.getCategory() != null) {
            category = getCategoryOrThrow(dto.getCategory());
        }

        if (dto.getStateAction() != null) {
            updateState(event, dto.getStateAction());
        }

        eventMapper.updateEventFromUserRequest(event, dto, category);
        Event updated = eventRepository.save(event);

        return eventMapper.toEventFullDto(updated);
    }

    private void updateState(Event event, String stateAction) {
        switch (stateAction) {
            case "CANCEL_REVIEW":
                if (!EventState.PENDING.equals(event.getState())) {
                    throw new ConflictException("Event can only be canceled in PENDING state");
                }
                event.setState(EventState.CANCELED);
                break;
            case "SEND_TO_REVIEW":
                if (!EventState.PENDING.equals(event.getState()) && !EventState.CANCELED.equals(event.getState())) {
                    throw new ConflictException("Event can only be sent for review from PENDING or CANCELED states");
                }
                event.setState(EventState.PENDING);
                break;
            default:
                throw new ValidationException("Invalid stateAction value: " + stateAction);
        }
    }

    private Event getEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " not found"));
    }

    private Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " not found"));
    }

    private User getUserOrThrow(Long userId) {
        return userClient.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));
    }

    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(HOURS_BEFORE_EVENT))) {
            throw new ConflictException(
                    "Event date cannot be earlier than " + HOURS_BEFORE_EVENT + " hours from now"
            );
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
}