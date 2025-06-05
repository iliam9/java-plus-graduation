package ru.practicum.ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentRequest;
import ru.practicum.ewm.dto.params.CommentParams;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.dto.ParamEventDto;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "users/{userId}")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@Valid @RequestBody NewCommentRequest request,
                                    @PathVariable @Positive long userId,
                                    @RequestParam @Positive long eventId) {
        log.info("Received request to add new comment: {}", request.getText());
        ParamEventDto params = new ParamEventDto(userId, eventId);
        return commentService.createComment(params, request);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive long userId,
                              @PathVariable @Positive long commentId) {
        log.info("Received request to delete comment with ID = {}", commentId);
        CommentParams params = new CommentParams(userId, commentId);
        commentService.deleteComment(params);
    }

    @GetMapping("/comments")
    public List<CommentDto> findUserComments(@PathVariable @Positive long userId) {
        log.info("Received request to find user ID = {} comments", userId);
        return commentService.findUserComments(userId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> findEventComments(@PathVariable @Positive long userId,
                                              @PathVariable @Positive long eventId) {
        log.info("Received request to find event ID = {} comments", eventId);
        ParamEventDto params = new ParamEventDto(userId, eventId);
        return commentService.findEventComment(params);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(@PathVariable @Positive long userId,
                                    @PathVariable @Positive long commentId,
                                    @Valid @RequestBody NewCommentRequest request) {
        log.info("Received request to update comment with ID = {}", commentId);
        CommentParams params = new CommentParams(userId, commentId);
        return commentService.updateComment(params, request);
    }


}
