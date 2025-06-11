package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.dto.events.CommentDto;
import ru.practicum.comments.service.CommentService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/comments")
public class CommentPrivateController {

    private final CommentService commentService;


    /**
     * Добавить новый комментарий на мероприятие.
     *
     * @param userId        идентификатор пользователя, добавляющего комментарий
     * @param eventId       идентификатор мероприятия, комментарий на которое добавляется
     * @param newCommentDto представление с информацией о добавляемом комментарии
     * @return представление только что добавленного комментария на мероприятие
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable("userId") long userId,
                                    @RequestParam long eventId,
                                    @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    /**
     * Обновить информацию о существующем комментарии на мероприятие.
     *
     * @param userId           идентификатор пользователя, обновляющего комментарий
     * @param commentId        идентификатор обновляемого комментария
     * @param updateCommentDto представление обновляемого комментария
     * @return представление обновленного комментария
     */
    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable("userId") long userId,
                                    @PathVariable("commentId") long commentId,
                                    @Valid @RequestBody NewCommentDto updateCommentDto) {
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    /**
     * Удалить комментарий на мероприятие.
     *
     * @param userId    идентификатор пользователя, удаляющего комментарий
     * @param commentId идентификатор удаляемого комментария
     */
    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable("userId") long userId,
                              @PathVariable("commentId") long commentId) {
        commentService.deleteComment(userId, commentId);
    }

}
