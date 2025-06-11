package ru.practicum.interaction.api.feignClient.client.event;
import feign.FeignException;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.api.UpdateObject;
import ru.practicum.interaction.api.dto.event.EventFullDto;
import ru.practicum.interaction.api.dto.event.UpdateEventAdminRequest;

@FeignClient(name = "event-service", path = "/admin/events")
public interface AdminEventClient {

    @PatchMapping("/{eventId}")
    EventFullDto updateEvent(@PositiveOrZero @PathVariable Long eventId,
                             @Validated(UpdateObject.class) @RequestBody UpdateEventAdminRequest updateEventAdminRequest) throws FeignException;


    @GetMapping("/{id}")
    EventFullDto findById(@PathVariable("id") @Positive Long id) throws FeignException;

    @PutMapping("/request/{eventId}")
    EventFullDto setConfirmedRequests(@PathVariable("eventId") Long eventId, @RequestBody Integer count) throws FeignException;
}
