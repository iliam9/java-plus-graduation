package ru.practicum.interaction.api.feignClient.client.request;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.dto.request.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/users/{userId}/requests")
public interface ParticipationRequestClient {

    @GetMapping
    List<ParticipationRequestDto> getAllUserRequests(@PathVariable Long userId) throws FeignException;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationRequestDto addRequest(@PathVariable Long userId,
                                       @RequestParam(defaultValue = "0") Long eventId) throws FeignException;

    @PatchMapping("/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) throws FeignException;
}
