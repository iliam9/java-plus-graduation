package ru.practicum.ewm.service;

import ru.practicum.interaction.api.enums.request.Status;
import ru.practicum.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;

import java.util.List;
import java.util.Map;

public interface ParticipationRequestService {

    ParticipationRequestDto addRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getAllUserRequests(Long userId);

    Map<Long, List<ParticipationRequest>> prepareConfirmedRequests(List<Long> eventIds);

    List<ParticipationRequestDto>  findAllByEventId(Long eventId);

    List<ParticipationRequestDto> findAllByIds(List<Long> ids);

    Map<Long, List<ParticipationRequestDto>> findAllConfirmedByEventId(List<Long> eventId);

    ParticipationRequestDto setStatusRequest(Long id, Status status);

    boolean checkExistsByEventIdAndRequesterIdAndStatus(Long eventId, Long userId, Status status);
}
