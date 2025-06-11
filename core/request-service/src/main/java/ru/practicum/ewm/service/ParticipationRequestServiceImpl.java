package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.UserActionClient;
import ru.practicum.ewm.grpc.stats.event.ActionTypeProto;
import ru.practicum.interaction.api.enums.request.Status;
import ru.practicum.interaction.api.exception.*;
import ru.practicum.interaction.api.exception.ValidationException;
import ru.practicum.interaction.api.feignClient.client.user.UserClient;
import ru.practicum.ewm.mapper.ParticipationRequestMapper;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.repository.ParticipationRequestRepository;
import ru.practicum.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.api.feignClient.client.event.AdminEventClient;
import ru.practicum.interaction.api.enums.event.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserClient userClient;
    private final AdminEventClient adminEventClient;
    private final UserActionClient userActionClient;

    @Transactional
    @Override
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {

        if (eventId == 0) {
            throw new ValidationException("Не задано id события");
        }

        var event = adminEventClient.findById(eventId);

        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new DuplicateException("Такой запрос уже существует");
        }
        if (event.getInitiator().equals(userId)) {
            throw new ConflictDataException("Пользователь не может создать запрос на участие в своем же событии");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictDataException("Нельзя участвовать в неопубликованном событии");
        }
        Integer participantLimit = event.getParticipantLimit();
        Integer confirmedRequests = event.getConfirmedRequests();
        if (!participantLimit.equals(0) && (participantLimit <= confirmedRequests)) {
            throw new ConflictDataException("Лимит запросов на участие в событии уже достигнут");
        }
        Status status;
        if (participantLimit.equals(0) || !event.getRequestModeration()) {
            status = Status.CONFIRMED;
            adminEventClient.setConfirmedRequests(eventId, ++confirmedRequests);
        } else
            status = Status.PENDING;
        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requesterId(userId)
                .eventId(eventId)
                .status(status)
                .build();
        ParticipationRequestDto participationRequestDto = ParticipationRequestMapper.toParticipationRequestDto(requestRepository.save(participationRequest));

        userActionClient.collectUserAction(eventId, userId, ActionTypeProto.ACTION_REGISTER, Instant.now());

        return participationRequestDto;
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {

        checkExistsUserById(userId);
        ParticipationRequest request = requestRepository.findByRequesterIdAndId(userId, requestId)
                .orElseThrow(() -> new NotFoundException("У пользователя с id: " + userId +
                        " не найдено запроса с id: " + requestId));
        if (request.getStatus() == Status.CONFIRMED) {
            var event = adminEventClient.findById(request.getEventId());
            Integer confirmedRequests = event.getConfirmedRequests();
            adminEventClient.setConfirmedRequests(request.getEventId(), --confirmedRequests);
        }
        request.setStatus(Status.CANCELED);
        return ParticipationRequestMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getAllUserRequests(Long userId) {
        checkExistsUserById(userId);
        return ParticipationRequestMapper.toParticipationRequestDto(requestRepository.findAllByRequesterId(userId));
    }

    public Map<Long, List<ParticipationRequest>> prepareConfirmedRequests(List<Long> eventIds) {
        log.info("Получаем список подтверждённых запросов для всех событий.");

        List<ParticipationRequest> confirmedRequests = requestRepository.findConfirmedRequests(eventIds);

        Map<Long, List<ParticipationRequest>> result = new HashMap<>();

        for (ParticipationRequest request : confirmedRequests) {
            var eventId = request.getEventId();
            List<ParticipationRequest> list = result.get(eventId);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(request);
            result.put(eventId, list);
        }
        return result;
    }

    @Override
    public List<ParticipationRequestDto> findAllByEventId(Long eventId) {
        var result =  ParticipationRequestMapper.toParticipationRequestDto(requestRepository.findAllByEventId(eventId));
        return result;
    }

    @Override
    public List<ParticipationRequestDto> findAllByIds(List<Long> ids) {
        var result =  ParticipationRequestMapper.toParticipationRequestDto(requestRepository.findAllById(ids));
        return result;
    }

    @Override
    public Map<Long, List<ParticipationRequestDto>> findAllConfirmedByEventId(List<Long> eventIds) {
        log.info("Получаем список подтверждённых запросов для всех событий.");

        List<ParticipationRequest> confirmedRequests = requestRepository.findConfirmedRequests(eventIds);

        Map<Long, List<ParticipationRequestDto>> result = new HashMap<>();

        for (ParticipationRequest request : confirmedRequests) {
            var eventId = request.getEventId();
            List<ParticipationRequestDto> list = result.get(eventId);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(ParticipationRequestMapper.toParticipationRequestDto(request));
            result.put(eventId, list);
        }
        return result;
    }

    @Transactional
    @Override
    public ParticipationRequestDto setStatusRequest(Long id, Status status) {
        var result = requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(" не найдено запроса с id: " + id));
        result.setStatus(status);
        return ParticipationRequestMapper.toParticipationRequestDto(result);
    }

    @Override
    public boolean checkExistsByEventIdAndRequesterIdAndStatus(Long eventId, Long userId, Status status) {
        return requestRepository.existsByEventIdAndRequesterIdAndStatus(eventId, userId, status);
    }

    private void checkExistsUserById(Long userId) {
        if (userClient.findById(userId)== null) {
            throw new NotFoundException("Пользователь c id: " + userId + " не найден");
        }
    }
}
