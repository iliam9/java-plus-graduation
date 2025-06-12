package ru.practicum.request.service;

import com.google.protobuf.Timestamp;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.State;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.grpc.stats.ActionTypeProto;
import ru.practicum.grpc.stats.UserActionControllerGrpc;
import ru.practicum.grpc.stats.UserActionProto;
import ru.practicum.request.dto.*;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.Request;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.client.UserClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
    UserClient userClient;
    EventClient eventClient;
    RequestRepository requestRepository;
    RequestMapper requestMapper;
    UserActionControllerGrpc.UserActionControllerBlockingStub collectorClient;

    @Override
    public ParticipationRequestDto createParticipationRequest(long userId, long eventId) {

        log.info("Создание запроса на участие для пользователя {} и события {}", userId, eventId);

        EventFullDto event = eventClient.getPublicEventById(eventId);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConditionsNotMetException("Нельзя участвовать в неопубликованном событии");
        }

        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        checkParticipantLimit(event.getParticipantLimit(), getConfirmedRequests(eventId));

        checkUser(userId);

        RequestStatus status = RequestStatus.PENDING;
        if (event.getParticipantLimit() == 0) {
            status = RequestStatus.CONFIRMED;
        } else if (!event.isRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        }

        Request request = Request.builder()
                .event(event.getId())
                .requester(userId)
                .status(status)
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        try {
            request = requestRepository.save(request);
        } catch (DataIntegrityViolationException e) {
            throw new ConditionsNotMetException("Нельзя добавить повторный запрос на участие в событии");
        }

        Instant instant = Instant.now();
        collectorClient.collectUserAction(UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(ActionTypeProto.ACTION_REGISTER)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build())
                .build());

        log.info("Создан запрос на участие в событии c id: {}, пользователем с id: {}", eventId, userId);
        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllByParticipantId(long userId) {
        log.info("Получение списка событий по id пользователя: {}", userId);
        List<Request> foundRequests = requestRepository.findAllByRequester(userId);
        return requestMapper.toDtoList(foundRequests);
    }

    @Override
    public ParticipationRequestDto cancelParticipantRequest(long userId, long requestId) {
        log.info("Отмена регистрации в событии с id: {}, пользователем с id: {}", requestId, userId);
        Request request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException(String.format("Запрос на участие в событии с id запроса=%d не найден", requestId))
        );

        Long requesterId = request.getRequester();
        if (!requesterId.equals(userId)) {
            throw new ConditionsNotMetException("Пользователь не является участником в запросе на участие в событии");
        }

        request.setStatus(RequestStatus.CANCELED);
        requestRepository.save(request);

        return requestMapper.toDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllByEventId(long eventId, long userId) {
        log.info("Получение всех заявок для события с id: {}, пользователем с id: {}", eventId, userId);
        checkUser(userId);
        return requestMapper.toDtoList(requestRepository.findAllByEvent(eventId));
    }

    @Override
    public EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(Long userId,
                                                                               RequestStatusUpdateDto updateDto) {
        log.info("Изменение статуса запросов на события по инициаторам {}", userId);

        checkUser(userId);

        EventRequestStatusUpdateRequest updateRequest = updateDto.getUpdateRequest();
        EventFullDto event = updateDto.getEvent();

        List<Long> requestIds = updateRequest.getRequestIds();
        List<Request> foundRequests = requestRepository.findAllById(requestIds);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        for (Request request : foundRequests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException("Заявка должна находиться в ожидании");
            }
        }

        switch (updateRequest.getStatus()) {
            case CONFIRMED -> handleConfirmedRequests(event, foundRequests, result, confirmed, rejected);
            case REJECTED -> handleRejectedRequests(foundRequests, result, rejected);
        }

        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }

    private void updateStatus(RequestStatus status, List<Long> ids) {
        log.info("Обновление статуса на: {}, для запросов: {}", status, ids);
        requestRepository.updateStatus(status, ids);
    }

    private void handleConfirmedRequests(EventFullDto event, List<Request> foundRequests, EventRequestStatusUpdateResult result, List<ParticipationRequestDto> confirmed, List<ParticipationRequestDto> rejected) {
        int confirmedRequests = getConfirmedRequests(event.getId());
        int participantLimit = event.getParticipantLimit();
        if (participantLimit == 0 || !event.isRequestModeration()) {
            result.setConfirmedRequests(requestMapper.toDtoList(foundRequests));
            return;
        }
        checkParticipantLimit(participantLimit, confirmedRequests);
        for (Request request : foundRequests) {
            if (confirmedRequests >= participantLimit) {
                rejected.add(requestMapper.toDto(request));
                continue;
            }
            request.setStatus(RequestStatus.CONFIRMED);
            confirmed.add(requestMapper.toDto(request));
            ++confirmedRequests;
        }
        List<Long> confirmedRequestIds = confirmed.stream().map(ParticipationRequestDto::getId).toList();
        updateStatus(RequestStatus.CONFIRMED, confirmedRequestIds);
    }

    private void handleRejectedRequests(List<Request> foundRequests, EventRequestStatusUpdateResult result,
                                        List<ParticipationRequestDto> rejected) {
        for (Request request : foundRequests) {
            request.setStatus(RequestStatus.REJECTED);
            rejected.add(requestMapper.toDto(request));
        }
        List<Long> rejectedRequestIds = rejected.stream().map(ParticipationRequestDto::getId).toList();
        updateStatus(RequestStatus.REJECTED, rejectedRequestIds);
    }

    private void checkParticipantLimit(int participantLimit, int confirmedRequests) {
        if (confirmedRequests >= participantLimit && participantLimit != 0) {
            throw new ConditionsNotMetException("У события заполнен лимит участников");
        }
    }

    private int getConfirmedRequests(long eventId) {
        return requestRepository.findCountOfConfirmedRequestsByEventId(eventId);
    }

    private void checkUser(Long userId) {
        if (userClient.getUser(userId).getBody() == null) {
            throw new NotFoundException("Не найден пользователь с id: " + userId);
        }
    }
}