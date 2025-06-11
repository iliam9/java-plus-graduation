package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.client.CollectorClient;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.events.EventDto;
import ru.practicum.dto.requests.ParticipationRequestDto;
import ru.practicum.dto.users.UserDto;
import ru.practicum.exception.*;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Request;
import ru.practicum.model.RequestStatus;
import ru.practicum.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final UserClient userClient;
    private final EventClient eventClient;
    private final CollectorClient collectorClient;
    private final RequestRepository requestRepository;

    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId, HttpServletRequest request) {
        List<UserDto> user = userClient.getUsers(List.of(userId), 0, 1);
        if (user.isEmpty()) {
            throw new NotFoundException(String.format("User with id %s not found", userId));
        }

        return requestRepository.findByRequesterId(userId).stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }

    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        if (eventId == 0) {
            throw new OperationForbiddenException("Передан некорректный идентификатор мероприятия");
        }
        if (eventClient.findByIdAndInitiatorId(eventId, userId) != null) {
            throw new InitiatorRequestException(String.format("User with id %s is initiator for event with id %s",
                    userId, eventId));
        }
        if (!requestRepository.findByRequesterIdAndEventId(userId, eventId).isEmpty()) {
            throw new RepeatableUserRequestException(String.format("User with id %s already make request for event with id %s",
                    userId, eventId));
        }
        List<EventDto> events = eventClient.getEvents(List.of(eventId));
        if (events.isEmpty()) {
            throw new NotFoundException(String.format("Event with id %s not found", eventId));
        }
        if (!EventState.PUBLISHED.equals(events.get(0).getState())) {
            throw new NotPublishEventException(String.format("Event with id %s is not published", eventId));
        }
        Request request = new Request();
        request.setRequesterId(userClient.getUsers(List.of(userId), 0, 1).get(0).getId());
        request.setEventId(events.get(0).getId());

        Long confirmedRequestsAmount = requestRepository.countRequestsByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (events.get(0).getParticipantLimit() <= events.get(0).getConfirmedRequests() && events.get(0).getParticipantLimit() != 0) {
            throw new ParticipantLimitException(String.format("Participant limit for event with id %s id exceeded", eventId));
        }

        if (events.get(0).getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCreatedOn(LocalDateTime.now());
            return requestMapper.requestToParticipationRequestDto(requestRepository.save(request));
        }

        if (events.get(0).isRequestModeration()) {
            request.setStatus(RequestStatus.PENDING);
            request.setCreatedOn(LocalDateTime.now());
            return requestMapper.requestToParticipationRequestDto(requestRepository.save(request));
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
            request.setCreatedOn(LocalDateTime.now());
        }

        collectorClient.sendRegistration(userId, eventId);
        return requestMapper.requestToParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        Request cancellingRequest = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException(String.format("Request with id %s not found or unavailable " +
                        "for user with id %s", requestId, userId)));
        cancellingRequest.setStatus(RequestStatus.CANCELED);
        cancellingRequest = requestRepository.save(cancellingRequest);
        return requestMapper.requestToParticipationRequestDto(cancellingRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId, HttpServletRequest request) {
        List<EventDto> userEvents = eventClient.findAllByInitiatorId(userId);

        EventDto event = userEvents.stream().findFirst()
                .orElseThrow(() -> new ValidationException(String.format("User with id %s is not initiator of event with id %s",
                        userId, eventId)));
        return requestRepository.findByEventId(event.getId())
                .stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }

    @Override
    public EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventStatusUpdate,
                                                              HttpServletRequest request) {
        EventDto event = eventClient.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new NotFoundException(String.format("Event with id %s not found " +
                    "or unavailable for user with id %s", eventId, userId));
        }
        int participantLimit = event.getParticipantLimit();
        if (participantLimit == 0 || !event.isRequestModeration()) {
            throw new OperationUnnecessaryException(String.format("Requests confirm for event with id %s is not required",
                    eventId));
        }

        List<Long> requestIds = eventStatusUpdate.getRequestIds();
        List<Request> requests = requestIds.stream()
                .map(r -> requestRepository.findByIdAndEventId(r, eventId)
                        .orElseThrow(() -> new ValidationException(String.format("Request with id %s is not apply " +
                                "to user with id %s or event with id %s", r, userId, eventId))))
                .toList();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        long confirmedRequestsAmount;
        confirmedRequestsAmount = requestRepository.countRequestsByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (confirmedRequestsAmount >= participantLimit) {
            throw new ParticipantLimitException(String.format("Participant limit for event with id %s id exceeded", eventId));
        }
        for (Request currentRequest : requests) {
            if (currentRequest.getStatus().equals(RequestStatus.PENDING)) {
                if (eventStatusUpdate.getStatus().equals(RequestStatus.CONFIRMED)) {
                    if (confirmedRequestsAmount <= participantLimit) {
                        currentRequest.setStatus(RequestStatus.CONFIRMED);
                        ParticipationRequestDto confirmed = requestMapper
                                .requestToParticipationRequestDto(currentRequest);
                        confirmedRequests.add(confirmed);
                    } else {
                        currentRequest.setStatus(RequestStatus.REJECTED);
                        ParticipationRequestDto rejected = requestMapper
                                .requestToParticipationRequestDto(currentRequest);
                        rejectedRequests.add(rejected);
                    }
                } else {
                    currentRequest.setStatus(eventStatusUpdate.getStatus());
                    ParticipationRequestDto rejected = requestMapper
                            .requestToParticipationRequestDto(currentRequest);
                    rejectedRequests.add(rejected);
                }
            }
        }
        requestRepository.saveAll(requests);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);
        return result;
    }

    @Override
    public ParticipationRequestDto findByRequesterIdAndEventIdAndStatus(Long authorId, Long eventId, RequestStatus requestStatus) {
        Optional<ParticipationRequestDto> result = requestRepository.findByRequesterIdAndEventIdAndStatus(authorId, eventId, requestStatus)
                .map(requestMapper::requestToParticipationRequestDto);
        return result.orElse(null);
    }

    @Override
    public Long countRequestsByEventIdAndStatus(Long eventId, RequestStatus requestStatus) {
        return requestRepository.countRequestsByEventIdAndStatus(eventId, requestStatus);
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> idsList, RequestStatus requestStatus) {
        return requestRepository.findAllByEventIdInAndStatus(idsList, requestStatus).stream()
                .map(requestMapper::requestToParticipationRequestDto)
                .toList();
    }

}
