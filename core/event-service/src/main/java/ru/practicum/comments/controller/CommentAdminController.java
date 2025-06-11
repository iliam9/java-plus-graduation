package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.AdminUpdateCommentStatusDto;
import ru.practicum.dto.events.CommentDto;
import ru.practicum.dto.enums.CommentStatus;
import ru.practicum.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "admin/comments")
public class CommentAdminController {

    private final CommentService commentService;


    /**
     * Получить список комментариев на мероприятия, находящихся на рассмотрении администратора.
     */
    @GetMapping
    public List<CommentDto> adminPendigCommentList() {
        return commentService.adminPendigCommentList();
    }

    /**
     * Обновить статус комментария на мероприятия (подтверждено/отклонено администратором).
     *
     * @param commentId идентификатор комментария
     * @param dto       представление, содержащее новый статус комментария
     * @return представление обновленного комментария
     */
    @PatchMapping("/{commentId}")
    public CommentDto adminUpdateCommentStatus(@PathVariable("commentId") Long commentId,
                                               @Valid @RequestBody AdminUpdateCommentStatusDto dto) {
        return commentService.adminUpdateCommentStatus(commentId, dto);
    }


    //------------------------Внутренние эндпоинты для взаимодействия микросервисов между собой-------------------------

    /**
     * Получить комментарии по их статусу и идентификатору мероприятия.
     *
     * @param eventId       идентификатор мероприятия, комментарии на которое запрошены
     * @param commentStatus статус комментариев
     * @return список комментариев
     */
    @GetMapping("/byEventId/{eventId}/andCommentStatus")
    public List<CommentDto> findByEventIdAndStatus(@PathVariable Long eventId, @RequestParam CommentStatus commentStatus) {
        return commentService.findByEventIdAndStatus(eventId, commentStatus);
    }

    /**
     * Получить комментарии по их статусу и идентификаторам мероприятий.
     *
     * @param idsList       список идентификаторов мероприятий, комментарии на которые запрошены
     * @param commentStatus статус комментариев
     * @return список комментариев
     */
    @GetMapping("/all/byEventIdsAndCommentStatus")
    public List<CommentDto> findAllByEventIdInAndStatus(@RequestParam List<Long> idsList, @RequestParam CommentStatus commentStatus) {
        return commentService.findAllByEventIdInAndStatus(idsList, commentStatus);
    }

}
