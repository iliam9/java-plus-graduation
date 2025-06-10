package ru.practicum.ewm;

import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.ewm.dto.EventWithInitiatorDto;

@FeignClient(name = "event-service", path = "/events")
public interface EventServiceFeignClient {

    @GetMapping("/short")
    EventWithInitiatorDto findEventWithInitiator(@RequestParam long eventId) throws FeignException;
}
