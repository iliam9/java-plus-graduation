package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventRequestStatusUpdateRequest;
import ru.practicum.dto.EventRequestStatusUpdateResult;
import ru.practicum.dto.requests.ParticipationRequestDto;
import ru.practicum.model.RequestStatus;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}")
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;


    /**
     * Получить информацию о запросах текущего пользователя на участие в чужих мероприятиях.
     * В случае, если по заданным фильтрам не найдено ни одного запроса, возвращает пустой список.
     *
     * @param userId идентификатор текущего пользователя
     */
    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId, HttpServletRequest request) {
        return requestService.getUserRequests(userId, request);
    }

    /**
     * Добавить запрос от текущего пользователя на участие в мероприятии.
     * Нельзя добавить повторный запрос  (Код ошибки 409).
     * Инициатор мероприятия не может добавить запрос на участие в своём мероприятии (Код ошибки 409).
     * Нельзя участвовать в неопубликованном мероприятии (Код ошибки 409).
     * Если у мероприятия достигнут лимит запросов на участие - необходимо вернуть ошибку (Код ошибки 409).
     * Если для мероприятия отключена пре-модерация запросов на участие, то запрос должен автоматически перейти
     * в состояние подтвержденного.
     *
     * @param userId  идентификатор текущего пользователя
     * @param eventId идентификатор мероприятия
     */
    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addParticipationRequest(@PathVariable Long userId,
                                                           @RequestParam Long eventId) {
        return requestService.addParticipationRequest(userId, eventId);
    }

    /**
     * Отменить свой запрос на участие в мероприятии.
     *
     * @param userId    идентификатор текущего пользователя
     * @param requestId идентификатор запроса на участие
     */
    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }

    /**
     * Получить информацию о запросах на участие в мероприятии текущего пользователя.
     * В случае, если по заданным фильтрам не найдено ни одного запроса, возвращает пустой список.
     *
     * @param userId  идентификатор текущего пользователя
     * @param eventId идентификатор мероприятия
     * @return Найденые запросы на участие в мероприятии текущего пользователя.
     */
    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(@PathVariable Long userId, @PathVariable Long eventId,
                                                              HttpServletRequest request) {
        return requestService.getEventParticipants(userId, eventId, request);
    }

    /**
     * Изменить статус (подтвержден, отменен) запросов на участие в мероприятии текущего пользователя.
     * Если для мероприятия лимит запросов равен 0 или отключена пре-модерация запросов, то подтверждение запросов не требуется.
     * Нельзя подтвердить запрос, если уже достигнут лимит по запросам на данное мероприятие (Код ошибки 409).
     * Статус можно изменить только у запросов, находящихся в состоянии ожидания.
     * Если при подтверждении данного запроса, лимит запросов для мероприятия исчерпан, то все неподтверждённые запросы
     * необходимо отклонить.
     *
     * @param userId            идентификатор текущего пользователя
     * @param eventId           идентификатор мероприятия текущего пользователя
     * @param eventStatusUpdate новый статус для запросов на участие в мероприятии текущего пользователя
     * @return Результат подтверждения/отклонения запросов на участие в мероприятии
     */
    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest eventStatusUpdate,
                                                              HttpServletRequest request) {
        return requestService.changeRequestStatus(userId, eventId, eventStatusUpdate, request);
    }


    //------------------------Внутренние эндпоинты для взаимодействия микросервисов между собой-------------------------

    /**
     * Получить информацию о запросе на участие конкретного пользователя в конкретном мероприятии и в конкретном статусе.
     *
     * @param userId        идентфикатор пользователя, запрос на участие которого запрашивается
     * @param eventId       идентификатор мероприятия, запрос на участие в котором запрашивается
     * @param requestStatus статус запрашиваемого запроса на участие
     * @return представление конкретного запроса на участие в мероприятии
     */
    @GetMapping("/events/{eventId}/requests/byStatus")
    public ParticipationRequestDto findByRequesterIdAndEventIdAndStatus(@PathVariable Long userId,
                                                                        @PathVariable Long eventId,
                                                                        @RequestParam RequestStatus requestStatus) {
        return requestService.findByRequesterIdAndEventIdAndStatus(userId, eventId, requestStatus);
    }

    /**
     * Получить количество запросов на участие в конкретном мероприятии и в конкретном статусе.
     *
     * @param userId        идентификатор пользователя
     * @param eventId       идентификатор мероприятия, количество запросов на участие в котором запрашивается
     * @param requestStatus статус мероприятия, количество запросов на участие в котором запрашивается
     * @return количество запросов на участие в конкретном мероприятии
     */
    @GetMapping("/events/{eventId}/requestsCount")
    public Long countRequestsByEventIdAndStatus(@PathVariable Long userId,
                                                @PathVariable Long eventId,
                                                @RequestParam RequestStatus requestStatus) {
        return requestService.countRequestsByEventIdAndStatus(eventId, requestStatus);
    }

    /**
     * Получить список запросов на участие в конкретных мероприятиях и в конкретном статусе.
     *
     * @param idsList       список идентификаторов мероприятий, запросы на участие в которых запрашиваются
     * @param userId        идентификатор пользователя
     * @param requestStatus статус мероприятий, запросы на участие в которых запрашиваются
     * @return список запросов на участие в конкретных мероприятиях
     */
    @GetMapping("/events/requests")
    public List<ParticipationRequestDto> findAllByEventIdInAndStatus(@RequestParam List<Long> idsList,
                                                                     @PathVariable Long userId,
                                                                     @RequestParam RequestStatus requestStatus) {
        return requestService.findAllByEventIdInAndStatus(idsList, requestStatus);
    }

}
