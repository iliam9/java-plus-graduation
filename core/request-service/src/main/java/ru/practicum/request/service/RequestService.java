package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto createParticipationRequest(long userId, long eventId);

    List<ParticipationRequestDto> getAllByParticipantId(long userId);

    ParticipationRequestDto cancelParticipantRequest(long userId, long requestId);

    List<ParticipationRequestDto> getAllByEventId(long eventId, long userId);

    EventRequestStatusUpdateResult changeEventRequestsStatusByInitiator(Long userId, RequestStatusUpdateDto updateDto);
}