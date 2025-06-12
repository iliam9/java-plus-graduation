package ru.practicum.request.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {
    private final RequestService requestService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto create(@PathVariable(name = "userId") @Positive long userId,
                                          @RequestParam(name = "eventId") @Positive long eventId) {
        return requestService.createParticipationRequest(userId, eventId);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllByParticipantId(@PathVariable(name = "userId") @Positive long userId) {
        return requestService.getAllByParticipantId(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ParticipationRequestDto cancelParticipantRequest(@PathVariable(name = "userId") @Positive long userId,
                                                            @PathVariable(name = "requestId") @Positive long requestId) {
        return requestService.cancelParticipantRequest(userId, requestId);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getAllByEventId(@PathVariable(name = "userId") @Positive long userId,
                                                         @PathVariable @Positive long eventId) {
        return requestService.getAllByEventId(eventId, userId);
    }

    @PutMapping("/status")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable(name = "userId") @Positive long userId,
                                                              @RequestBody RequestStatusUpdateDto updateDto) {
        return requestService.changeEventRequestsStatusByInitiator(userId, updateDto);
    }
}