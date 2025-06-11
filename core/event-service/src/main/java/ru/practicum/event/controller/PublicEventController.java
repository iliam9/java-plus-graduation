package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.events.EventDto;
import ru.practicum.event.dto.EntityParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.model.EventSort;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@RequiredArgsConstructor
public class PublicEventController {

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final EventService eventService;


    /**
     * Получить мероприятие с возможностью фильтрации.
     * В выдаче - только опубликованные мероприятия.
     * Текстовый поиск (по аннотации и подробному описанию) - без учета регистра букв.
     * Если в запросе не указан диапазон дат [rangeStart-rangeEnd], то выгружаются мероприятия,
     * которые происходят позже текущей даты и времени.
     * Информация о каждом мероприятии включает в себя количество просмотров и количество уже одобренных запросов на участие.
     * Информация о том, что по эндпоинту был осуществлен и обработан запрос, сохраняется в сервисе статистики.
     * В случае, если по заданным фильтрам не найдено ни одного мероприятия, возвращается пустой список.
     *
     * @param text          текст для поиска в содержимом аннотации и подробном описании мероприятия
     * @param sort          Вариант сортировки: по дате мероприятия или по количеству просмотров
     * @param from          количество мероприятий, которые нужно пропустить для формирования текущего набора
     * @param size          количество мероприятий в наборе
     * @param categories    список идентификаторов категорий в которых будет вестись поиск
     * @param rangeStart    дата и время не раньше которых должно произойти мероприятие
     * @param rangeEnd      дата и время не позже которых должно произойти мероприятие
     * @param paid          поиск только платных/бесплатных мероприятий
     * @param onlyAvailable только мероприятия у которых не исчерпан лимит запросов на участие
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getEvents(@RequestParam(required = false) @Size(min = 1, max = 7000) String text,
                                         @RequestParam(required = false) EventSort sort,
                                         @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
                                         @RequestParam(required = false, defaultValue = "10") int size,
                                         @RequestParam(required = false) List<Long> categories,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeStart,
                                         @RequestParam(required = false) @DateTimeFormat(pattern = DATE_PATTERN) LocalDateTime rangeEnd,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable,
                                         HttpServletRequest request) {
        EntityParam params = new EntityParam();
        params.setText(text);
        params.setSort(sort);
        params.setFrom(from);
        params.setSize(size);
        params.setCategories(categories);
        params.setRangeStart(rangeStart);
        params.setRangeEnd(rangeEnd);
        params.setPaid(paid);
        params.setOnlyAvailable(onlyAvailable);

        return eventService.getEvents(params);
    }

    /**
     * Получить подробную информацию об опубликованном мероприятии по его идентификатору.
     * Мероприятие должно быть опубликовано.
     * Информация о мероприятии должна включать в себя количество просмотров и количество подтвержденных запросов.
     * Информация о том, что по эндпоинту был осуществлен и обработан запрос, сохраняется в сервисе статистики.
     *
     * @param id id мероприятия
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EventDto getEvent(@RequestHeader("X-EWM-USER-ID") long userId, @PathVariable Long id, HttpServletRequest request) {
        EventDto result = eventService.getEvent(userId, id);
        return result;
    }

    /**
     * Получить список мероприятий, рекомендованных пользователю, на основании его предыдущей активности.
     *
     * @param userId идентификатор пользователя
     */
    @GetMapping("/recommendations")
    public List<EventDto> getRecommendedEventsForUser(@RequestHeader("X-EWM-USER-ID") long userId) {
        return eventService.getRecommendedEventsForUser(userId);
    }

    /**
     * Постановка лайка пользователем конкретному, посещенному им мероприятию.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор мероприятия
     */
    @PutMapping("/{eventId}/like")
    public void addLikeToEvent(@RequestHeader("X-EWM-USER-ID") long userId, @PathVariable Long eventId) {
        eventService.addLikeToEvent(userId, eventId);
    }


    //------------------------Внутренние эндпоинты для взаимодействия микросервисов между собой-------------------------

    /**
     * Получить список мероприятий по их идентификаторам.
     *
     * @param ids список идентификаторов запрашиваемых мероприятий
     */
    @GetMapping("/byIds")
    public List<EventDto> getEvents(@RequestParam(required = false) List<Long> ids) {
        return eventService.getEvents(ids);
    }

    /**
     * Получить список мероприятий, принадлежащих конкретной категории (разделу).
     *
     * @param categoryId идентификатор категории (раздела), мероприятия из которой запрашиваются
     * @return список мероприятий конкретной категории (раздела)
     */
    @GetMapping("/byCategoryId/{categoryId}")
    public List<EventDto> findAllByCategoryId(@PathVariable Long categoryId) {
        return eventService.findAllByCategoryId(categoryId);
    }

    /**
     * Получить конкретное мероприятие по его идентификатору и идентификатору создателя данного мероприятия.
     *
     * @param eventId идентификатор мероприятия
     * @param userId  идентификатор пользователя, создавшего запрашиваемое мероприятие
     * @return представление запрошенного мероприятия
     */
    @GetMapping("/byId/{eventId}/andInitiatorId/{userId}")
    public EventDto findByIdAndInitiatorId(@PathVariable Long eventId, @PathVariable Long userId) {
        return eventService.findByIdAndInitiatorId(eventId, userId);
    }

    /**
     * Получить список всех мероприятий, созданных конкретным польователем.
     *
     * @param initiatorId идентификатор пользователя, создавшего запрашиваемые мероприятия
     * @return список всех мероприятий конкретного пользователя
     */
    @GetMapping("/all/byInitiatorId/{initiatorId}")
    public List<EventDto> findAllByInitiatorId(@PathVariable Long initiatorId) {
        return eventService.findAllByInitiatorId(initiatorId);
    }

}
