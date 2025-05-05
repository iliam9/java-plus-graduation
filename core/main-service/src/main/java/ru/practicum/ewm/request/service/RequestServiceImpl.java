package ru.practicum.ewm.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.errorHandler.exception.ConflictDataException;
import ru.practicum.ewm.errorHandler.exception.NotFoundException;
import ru.practicum.ewm.event.dto.ParamEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> findRequest(long userId) {
        User user = getUser(userId);
        List<Request> requests = requestRepository.findAllByUser(user);
        return requestMapper.mapToDto(requests);
    }

    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        User user = getUser(userId);
        Event event = getEvent(eventId);
        validateRequest(user, event);
        Request request = new Request(event, user);
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        requestRepository.save(request);
        return requestMapper.mapToDto(request);
    }

    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        User user = getUser(userId);
        Request request = getRequest(requestId, user);
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        return requestMapper.mapToDto(request);
    }

    @Override
    public List<ParticipationRequestDto> findRequest(ParamEventDto paramEventDto) {
        Event event = getUserEvent(paramEventDto);
        List<Request> requests = requestRepository.findAllByEvent(event);
        return requestMapper.mapToDto(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequest(ParamEventDto paramEventDto,
                                                        EventRequestStatusUpdateRequest updateRequest) {
        Event event = getUserEvent(paramEventDto);
        List<Request> requests = requestRepository.findAllByEvent(event);
        RequestStatus status = updateRequest.getStatus();
        List<Request> updatedRequests = new ArrayList<>();
        switch (status) {
            case REJECTED -> updatedRequests = rejectRequest(requests, updateRequest);
            case CONFIRMED -> updatedRequests = confirmRequest(event, requests, updateRequest);
        }
        requestRepository.saveAll(updatedRequests);
        List<Request> confirmedRequests = updatedRequests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED)).toList();
        List<Request> rejectedRequests = updatedRequests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.REJECTED)).toList();
        return requestMapper.mapToRequestStatus(confirmedRequests, rejectedRequests);
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Not found user with ID = {}", userId);
                    return new NotFoundException(String.format("Not found user with ID = %d", userId));
                });
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
        User user = getUser(userId);
        Event event = getEvent(eventId);
        if (event.getInitiator() != user) {
            log.error("Not found event with ID = {}", eventId);
            throw new NotFoundException(
                    String.format("Not found event with ID = %d", eventId));
        }
        return event;
    }

    private Request getRequest(long requestId, User user) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Not found request with ID = {}", requestId);
                    return new NotFoundException(String.format("Not found request with ID = %d", requestId));
                });
        isUsersRequest(user, request);
        return request;
    }

    private void validateRequest(User user, Event event) {
        List<Request> requests = requestRepository.findAllByEvent(event);
        isRepeatedRequest(user, requests);
        isUserEqualsEventInitiator(user, event);
        isEventPublished(event);
        isRequestLimitReached(event, requests);
    }

    private void isRepeatedRequest(User user, List<Request> requests) {
        if (requests.stream().anyMatch(request -> request.getUser() == user)) {
            log.error("Request by user ID = {}  is repeated", user.getId());
            throw  new ConflictDataException(
                    String.format("Request by user ID = %d  is repeated", user.getId()));
        }
    }

    private void isUserEqualsEventInitiator(User user, Event event) {
        if (event.getInitiator().equals(user)) {
            log.error("User ID = {} is initiator of event ID = {}", user.getId(), event.getId());
            throw new ConflictDataException(
                    String.format("User ID = %d is initiator of event ID = %d", user.getId(), event.getId()));
        }
    }

    private void isEventPublished(Event event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Event ID = {} is not published", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d is not published", event.getId()));
        }
    }

    private void isRequestLimitReached(Event event, List<Request> requests) {
        long count = requests.stream().filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED)).count();
        long limit = event.getParticipantLimit();
        if (limit != 0 && count + 1 > limit) {
            log.error("Event ID = {} request limit is reached", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d request limit is reached", event.getId()));
        }
    }

    private void isUsersRequest(User user, Request request) {
        if (request.getUser() != user) {
            log.error("User ID = {} is not own request ID = {}", user.getId(), request.getId());
            throw new NotFoundException(
                    String.format("Event ID = %d is not own request ID = %d", user.getId(), request.getId()));
        }
    }

    private void checkEventForRequestLimit(Event event,
                                           List<Request> requests,
                                           EventRequestStatusUpdateRequest updateRequest) {
        long limit = event.getParticipantLimit();
        if (limit == 0) {
            return;
        }
        long countUpdateRequest = updateRequest.getRequestIds().size();
        long countConfirmedRequests = requests.stream()
                .filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED)).count();
        if (countConfirmedRequests + countUpdateRequest > limit) {
            log.error("Event ID = {} request limit is reached", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d request limit is reached", event.getId()));
        }
    }

    private void checkConfirmedRequest(List<Request> requests,
                                       EventRequestStatusUpdateRequest updateRequest) {
        boolean match = requests.stream().anyMatch(request -> updateRequest.getRequestIds().contains(request.getId())
                && request.getStatus().equals(RequestStatus.CONFIRMED));
        if (match) {
            log.error("Confirmed request canceled {}", updateRequest);
            throw new ConflictDataException("Confirmed request canceled");
        }
    }

    private List<Request> rejectRequest(List<Request> requests,
                                        EventRequestStatusUpdateRequest updateRequest) {
        checkConfirmedRequest(requests, updateRequest);
        return requests.stream()
                .map(request -> {
                            if (updateRequest.getRequestIds().contains(request.getId())) {
                                request.setStatus(RequestStatus.REJECTED);
                            }
                            return request;
                        }
                ).toList();
    }

    private List<Request> confirmRequest(Event event,
                                         List<Request> requests,
                                         EventRequestStatusUpdateRequest updateRequest) {
        checkEventForRequestLimit(event, requests, updateRequest);
        return requests.stream()
                .map(request -> {
                            if (updateRequest.getRequestIds().contains(request.getId())) {
                                request.setStatus(RequestStatus.CONFIRMED);
                            }
                            return request;
                        }
                ).toList();
    }

}
