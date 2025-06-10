package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.RequestServiceFeignClient;
import ru.practicum.ewm.dto.ParamEventDto;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.RequestCountDto;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class PrivateRequestController implements RequestServiceFeignClient {
    private final RequestService requestService;

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> findEventRequest(@Positive @PathVariable long userId,
                                                          @Positive @PathVariable long eventId) {
        ParamEventDto paramEventDto = new ParamEventDto(userId, eventId);
        log.info("Request to find eventRequests {}", paramEventDto);
        return requestService.findRequest(paramEventDto);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequest(@Positive @PathVariable long userId,
                                                             @Positive @PathVariable long eventId,
                                                             @RequestBody EventRequestStatusUpdateRequest updateEvent) {
        ParamEventDto paramEventDto = new ParamEventDto(userId, eventId);
        log.info("Request to update eventRequests {}", paramEventDto);
        return requestService.updateRequest(paramEventDto, updateEvent);
    }

    @Override
    public List<RequestCountDto> findConfirmedRequest(List<Long> ids) {
        return requestService.findConfirmedRequest(ids);
    }

    @Override
    public boolean isUserParticipatedInEvent(long userId, long eventId) {
        return requestService.isUserParticipatedInEvent(userId, eventId);
    }
}
