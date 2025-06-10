package ru.practicum.ewm;

import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.RequestCountDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/users")
public interface RequestServiceFeignClient {

    @GetMapping("/requests")
    List<RequestCountDto> findConfirmedRequest(@RequestParam List<Long> ids);

    @GetMapping("/requests/confirmed")
    boolean isUserParticipatedInEvent(@Positive @RequestParam long userId, @Positive @RequestParam long eventId);
}
