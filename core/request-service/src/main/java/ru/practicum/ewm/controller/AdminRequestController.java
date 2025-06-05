package ru.practicum.ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.ParticipationRequestDto;
import ru.practicum.ewm.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users")
public class AdminRequestController {
    private final RequestService requestService;

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findRequest(@Positive @PathVariable long userId) {
        log.info("Request to find requests by user ID = {}", userId);
        return requestService.findRequest(userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@Positive @PathVariable long userId,
                                                 @Positive @RequestParam long eventId) {
        log.info("Request to create request by user ID = {} to event ID = {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@Positive @PathVariable long userId,
                                                 @Positive @PathVariable long requestId) {
        log.info("Request to cancellation of the request ID = {}", requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}
