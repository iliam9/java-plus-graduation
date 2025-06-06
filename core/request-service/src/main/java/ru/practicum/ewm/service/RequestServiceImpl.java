package ru.practicum.ewm.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.EventServiceFeignClient;
import ru.practicum.ewm.UserServiceFeignClient;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.EventWithInitiatorDto;
import ru.practicum.ewm.dto.ParamEventDto;
import ru.practicum.ewm.dto.RequestCountDto;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.exception.ConflictDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.dto.EventState;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.Request;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventServiceFeignClient eventClient;
    private final UserServiceFeignClient userClient;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> findRequest(long userId) {
        List<Request> requests = requestRepository.findAllByUserId(userId);
        return requestMapper.mapToDto(requests);
    }

    @Override
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        UserShortDto user = getUser(userId);
        EventWithInitiatorDto event = getEvent(eventId);
        validateRequest(user, event);
        Request request = new Request(event.getId(), user.getId());
        if (!event.isRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        requestRepository.save(request);
        log.info("Save request ID = {}", request.getId());
        return requestMapper.mapToDto(request);
    }

    @Override
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        Request request = getRequest(requestId, userId);
        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);
        log.info("Canceled request ID = {}", request.getId());
        return requestMapper.mapToDto(request);
    }

    @Override
    public List<ParticipationRequestDto> findRequest(ParamEventDto paramEventDto) {
        List<Request> requests = requestRepository.findAllByEventId(paramEventDto.getEventId());
        return requestMapper.mapToDto(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequest(ParamEventDto paramEventDto,
                                                        EventRequestStatusUpdateRequest updateRequest) {
        EventWithInitiatorDto event = getUserEvent(paramEventDto);
        List<Request> requests = requestRepository.findAllByEventId(event.getId());
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
        log.info("Update request of event ID = {}", event.getId());
        return requestMapper.mapToRequestStatus(confirmedRequests, rejectedRequests);
    }

    @Override
    public List<RequestCountDto> findConfirmedRequest(List<Long> ids) {
        List<RequestCountDto> requests = requestRepository.findConfirmedRequest(ids);
        return requests;
    }

    private UserShortDto getUser(long userId) {
        try {
            return userClient.findShortUsers(List.of(userId)).getFirst();
        } catch (FeignException e) {
            log.error("Not found user with ID = {}", userId);
            throw new NotFoundException(String.format("Not found user with ID = %d", userId));
        }
    }

    private EventWithInitiatorDto getEvent(long eventId) {
        try {
            return eventClient.findEventWithInitiator(eventId);
        } catch (FeignException e) {
            log.error("Not found event with ID = {}", eventId);
            throw  new NotFoundException(String.format("Not found event with ID = %d", eventId));
        }
    }

    private EventWithInitiatorDto getUserEvent(ParamEventDto paramEventDto) {
        long userId = paramEventDto.getUserId();
        long eventId = paramEventDto.getEventId();
        EventWithInitiatorDto event = getEvent(eventId);
        if (event.getInitiatorId() != userId) {
            log.error("Not found event with ID = {}", eventId);
            throw new NotFoundException(
                    String.format("Not found event with ID = %d", eventId));
        }
        return event;
    }

    private Request getRequest(long requestId, long userId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Not found request with ID = {}", requestId);
                    return new NotFoundException(String.format("Not found request with ID = %d", requestId));
                });
        isUsersRequest(userId, request);
        return request;
    }

    private void validateRequest(UserShortDto user, EventWithInitiatorDto event) {
        List<Request> requests = requestRepository.findAllByEventId(event.getId());
        isRepeatedRequest(user, requests);
        isUserEqualsEventInitiator(user, event);
        isEventPublished(event);
        isRequestLimitReached(event, requests);
    }

    private void isRepeatedRequest(UserShortDto user, List<Request> requests) {
        if (requests.stream().anyMatch(request -> request.getUserId().equals(user.getId()))) {
            log.error("Request by user ID = {}  is repeated", user.getId());
            throw  new ConflictDataException(
                    String.format("Request by user ID = %d  is repeated", user.getId()));
        }
    }

    private void isUserEqualsEventInitiator(UserShortDto user, EventWithInitiatorDto event) {
        if (event.getInitiatorId() == user.getId()) {
            log.error("User ID = {} is initiator of event ID = {}", user.getId(), event.getId());
            throw new ConflictDataException(
                    String.format("User ID = %d is initiator of event ID = %d", user.getId(), event.getId()));
        }
    }

    private void isEventPublished(EventWithInitiatorDto event) {
        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.error("Event ID = {} is not published", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d is not published", event.getId()));
        }
    }

    private void isRequestLimitReached(EventWithInitiatorDto event, List<Request> requests) {
        long count = requests.stream().filter(request -> request.getStatus().equals(RequestStatus.CONFIRMED)).count();
        long limit = event.getParticipantLimit();
        if (limit != 0 && count + 1 > limit) {
            log.error("Event ID = {} request limit is reached", event.getId());
            throw new ConflictDataException(
                    String.format("Event ID = %d request limit is reached", event.getId()));
        }
    }

    private void isUsersRequest(long userId, Request request) {
        if (request.getUserId() != userId) {
            log.error("User ID = {} is not own request ID = {}", userId, request.getId());
            throw new NotFoundException(
                    String.format("Event ID = %d is not own request ID = %d", userId, request.getId()));
        }
    }

    private void checkEventForRequestLimit(EventWithInitiatorDto event,
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

    private List<Request> confirmRequest(EventWithInitiatorDto event,
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
