package ru.practicum.interaction.api.feignClient.client.request;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.enums.request.Status;
import ru.practicum.interaction.api.dto.request.ParticipationRequestDto;
import ru.practicum.interaction.api.exception.ServerErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FeignClient(name = "request-service", path = "/requests")
public interface AdminParticipationRequestClient {

    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "findAllByEventIdFallback")
    @GetMapping("/event/{eventId}")
    List<ParticipationRequestDto> findAllByEventId(@PathVariable Long eventId) throws FeignException;

    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "findAllByIdsFallback")
    @GetMapping("/{ids}")
    List<ParticipationRequestDto> findAllByIds(@PathVariable List<Long> ids) throws FeignException;

    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "findAllConfirmedByEventIdFallback")
    @GetMapping("/event/confirmed/{eventId}")
    Map<Long, List<ParticipationRequestDto>> findAllConfirmedByEventId(@PathVariable List<Long> eventId) throws FeignException;

    @CircuitBreaker(name = "defaultBreaker", fallbackMethod = "setStatusRequestFallback")
    @PutMapping("/status/{id}/{status}")
    ParticipationRequestDto setStatusRequest(@PathVariable Long id, @PathVariable Status status)  throws FeignException;

    @GetMapping("/{eventId}/check-user-confirmed/{userId}")
    boolean checkExistStatusRequest(@PathVariable Long eventId,@PathVariable Long userId,
                                    @RequestParam Status status);

    @GetMapping("/event/{eventId}")
    default List<ParticipationRequestDto> findAllByEventIdFallback(Long eventId, Throwable throwable) {
        return new ArrayList<>();
    }

    @GetMapping("/{ids}")
    default List<ParticipationRequestDto> findAllByIdsFallback(List<Long> ids, Throwable throwable) {
        return new ArrayList<>();
    }

    @GetMapping("/event/confirmed/{eventId}")
    default Map<Long, List<ParticipationRequestDto>> findAllConfirmedByEventIdFallback(List<Long> eventId, Throwable throwable) {
        return new HashMap<>();
    }

    @PutMapping("/status/{id}/{status}")
    default ParticipationRequestDto setStatusRequestFallback(Long id, Status status) {
        throw new ServerErrorException("Server Error Exception.");
    }
}

