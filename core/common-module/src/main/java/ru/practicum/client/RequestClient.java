package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.client.fallback.RequestClientFallback;
import ru.practicum.dto.enums.RequestStatus;
import ru.practicum.dto.requests.ParticipationRequestDto;

import java.util.List;


@FeignClient(name = "request-service", fallback = RequestClientFallback.class)
public interface RequestClient {

    /**
     * Получение запроса на участие в конкретном мероприятии с конкретным статусом от конкретного пользователя.
     *
     * @param userId        идентификатор пользователя
     * @param eventId       идентификатор мероприятия
     * @param requestStatus требуемый статус мероприятия
     * @return запрос на участие в мероприятии.
     */
    @GetMapping("/users/{userId}/events/{eventId}/requests/byStatus")
    ParticipationRequestDto findByRequesterIdAndEventIdAndStatus(@PathVariable Long userId,
                                                                 @PathVariable Long eventId,
                                                                 @RequestParam RequestStatus requestStatus);

    /**
     * Получить количество запросов на участие в конкретном мероприятии с конкретным статусом.
     *
     * @param userId        идентификатор пользователя
     * @param eventId       идентификатор мероприятия
     * @param requestStatus требуемый статус мероприятия
     * @return количество запросов на участие в мероприятии.
     */
    @GetMapping("/users/{userId}/events/{eventId}/requestsCount")
    Long countRequestsByEventIdAndStatus(@PathVariable Long userId,
                                         @PathVariable Long eventId,
                                         @RequestParam RequestStatus requestStatus);

    /**
     * Получить все запросы на участие в конкретных мероприятиях с конкретным статусом.
     *
     * @param idsList       список идентификаторов мероприятий
     * @param userId        идентификатор пользователя
     * @param requestStatus требуемый статус мероприятий
     * @return список запросов на участие в мероприятиях.
     */
    @GetMapping("/users/{userId}/events/requests")
    List<ParticipationRequestDto> findAllByEventIdInAndStatus(@RequestParam List<Long> idsList,
                                                              @PathVariable Long userId,
                                                              @RequestParam RequestStatus requestStatus);

}
