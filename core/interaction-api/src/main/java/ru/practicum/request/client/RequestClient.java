package ru.practicum.request.client;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/users", fallbackFactory = RequestClientFallbackFactory.class)
public interface RequestClient {

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto create(@PathVariable(name = "userId") @Positive long userId,
                                   @RequestParam(name = "eventId") @Positive long eventId);

    @GetMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.OK)
    List<ParticipationRequestDto> getAllByParticipantId(@PathVariable(name = "userId") @Positive long userId);

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    @ResponseStatus(HttpStatus.OK)
    ParticipationRequestDto cancelParticipantRequest(@PathVariable(name = "userId") @Positive long userId,
                                                     @PathVariable(name = "requestId") @Positive long requestId);

    @GetMapping("/{userId}/requests/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    List<ParticipationRequestDto> getAllByEventId(@PathVariable(name = "userId") @Positive long userId,
                                                         @PathVariable @Positive long eventId);

    @PutMapping("/{userId}/requests/status")
    EventRequestStatusUpdateResult updateRequestStatus(@PathVariable(name = "userId") @Positive long userId,
                                                       @RequestBody RequestStatusUpdateDto requestStatusUpdateDto);
}