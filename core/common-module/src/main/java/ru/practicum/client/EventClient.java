package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.client.fallback.EventClientFallback;
import ru.practicum.dto.events.EventDto;

import java.util.List;

@FeignClient(name = "event-service", fallback = EventClientFallback.class)
public interface EventClient {

    /**
     * Получение мероприятий по их идентификаторам.
     *
     * @param ids список идентификаторов запрашиваемых мероприятий
     * @return список мероприятий.
     */
    @GetMapping("/events/byIds")
    List<EventDto> getEvents(@RequestParam(required = false) List<Long> ids);

    /**
     * Получение всех мероприятий, относящихся к конкретной категории по идентификатору данной категории.
     *
     * @param categoryId идентификатор категории, по которой запрашиваются мероприятия
     * @return список мероприятий конкретной категории.
     */
    @GetMapping("/events/byCategoryId/{categoryId}")
    List<EventDto> findAllByCategoryId(@PathVariable Long categoryId);

    /**
     * Получение мероприятия по его идентификатору и инициатору.
     *
     * @param eventId идентификатор мероприятия
     * @param userId  идентификатор инициатора мероприятия
     * @return конкретное мероприятие.
     */
    @GetMapping("/events/byId/{eventId}/andInitiatorId/{userId}")
    EventDto findByIdAndInitiatorId(@PathVariable Long eventId, @PathVariable Long userId);

    /**
     * Получение всех мероприятий, созданных конкретным пользователем.
     *
     * @param initiatorId идентификатор пользователя
     * @return список мероприятий, созданных пользователем.
     */
    @GetMapping("/events/all/byInitiatorId/{initiatorId}")
    List<EventDto> findAllByInitiatorId(@PathVariable Long initiatorId);

}
