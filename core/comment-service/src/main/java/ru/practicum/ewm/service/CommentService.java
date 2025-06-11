package ru.practicum.ewm.service;

import ru.practicum.interaction.api.dto.comment.CommentDto;
import ru.practicum.interaction.api.dto.comment.NewCommentDto;
import ru.practicum.interaction.api.dto.user.UserDtoForAdmin;
import ru.practicum.interaction.api.enums.comment.SortType;

import java.util.List;

public interface CommentService {

    CommentDto createComment(Long eventId, Long userId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long eventId, Long commentId);

    void deleteComment(Long commentId, Long eventId);

    List<CommentDto> getAllComments(Long eventId, SortType sortType, Integer from, Integer size);

    CommentDto addLike(Long userId, Long commentId);

    UserDtoForAdmin addBanCommited(Long userId, Long eventId);

    void deleteBanCommited(Long userId, Long eventId);

    void deleteLike(Long userId, Long commentId);

    CommentDto getComment(Long id);
}
