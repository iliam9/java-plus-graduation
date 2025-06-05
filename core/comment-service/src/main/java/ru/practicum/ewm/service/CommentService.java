package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.CommentDto;
import ru.practicum.ewm.dto.NewCommentRequest;
import ru.practicum.ewm.dto.params.CommentParams;
import ru.practicum.ewm.dto.ParamEventDto;

import java.util.List;

public interface CommentService {

    CommentDto createComment(ParamEventDto params, NewCommentRequest request);

    void deleteComment(CommentParams params);

    List<CommentDto> findEventComment(ParamEventDto paramEventDto);

    List<CommentDto> findUserComments(long userId);

    CommentDto updateComment(CommentParams params, NewCommentRequest request);
}
