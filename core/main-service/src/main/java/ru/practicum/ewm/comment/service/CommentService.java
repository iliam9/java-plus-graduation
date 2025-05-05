package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentRequest;
import ru.practicum.ewm.comment.dto.params.CommentParams;
import ru.practicum.ewm.event.dto.ParamEventDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(ParamEventDto params, NewCommentRequest request);

    void deleteComment(CommentParams params);

    List<CommentDto> findEventComment(ParamEventDto paramEventDto);

    List<CommentDto> findUserComments(long userId);

    CommentDto updateComment(CommentParams params, NewCommentRequest request);
}
