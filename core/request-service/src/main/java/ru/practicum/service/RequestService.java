package ru.practicum.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.requests.ParticipationRequestDto;
import ru.practicum.model.RequestStatus;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId, HttpServletRequest request);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId, HttpServletRequest request);

    EventRequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest eventStatusUpdate,
                                                       HttpServletRequest request);

    ParticipationRequestDto findByRequesterIdAndEventIdAndStatus(Long authorId, Long eventId, RequestStatus requestStatus);

    Long countRequestsByEventIdAndStatus(Long eventId, RequestStatus requestStatus);

    List<ParticipationRequestDto> findAllByEventIdInAndStatus(List<Long> idsList, RequestStatus requestStatus);
}
