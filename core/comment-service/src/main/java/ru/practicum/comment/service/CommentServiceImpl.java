package ru.practicum.comment.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.client.EventClient;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.client.UserClient;
import ru.practicum.user.dto.UserShortDto;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventClient eventClient;
    private final UserClient userClient;

    @Override
    public List<CommentDto> getComments(Long eventId) {
        log.info("Get comments by eventId: {}", eventId);
        eventClient.getEvent(eventId);

        List<CommentDto> dtos = CommentMapper.INSTANCE.toDtos(commentRepository.findAllByEvent(eventId));
        return dtos.stream()
                // Избегаем дублирования комментариев в ответе
                .filter((dto) -> dto.getParentCommentId() == null)
                .toList();
    }

    @Override
    public CommentDto createComment(Long userId, CommentDto commentDto) {
        UserShortDto user = userClient.getUser(userId).getBody();
        EventFullDto event = eventClient.getEvent(commentDto.getEventId());
        Comment comment = CommentMapper.INSTANCE.toEntity(commentDto);
        comment.setUser(userId);
        comment.setEvent(event.getId());
        log.info("Create comment: {}", comment);
        return CommentMapper.INSTANCE.toDto(commentRepository.save(comment), user);
    }

    @Override
    public CommentDto updateComment(CommentDto commentDto) {
        EventFullDto event = eventClient.getEvent(commentDto.getEventId());
        Comment comment = getCommentById(commentDto.getId());
        CommentMapper.INSTANCE.updateDto(commentDto, comment);
        log.info("Admin update comment: {}", comment);
        comment.setEvent(event.getId());
        return CommentMapper.INSTANCE.toDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(Long userId, CommentDto commentDto) {
        getUser(userId);
        EventFullDto event = eventClient.getEvent(commentDto.getEventId());
        Comment comment = getCommentById(commentDto.getId());
        CommentMapper.INSTANCE.updateDto(commentDto, comment);
        log.info("Update comment: {}", comment);
        comment.setUser(userId);
        comment.setEvent(event.getId());
        return CommentMapper.INSTANCE.toDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {
        getUser(userId);
        if (commentRepository.existsById(commentId)) {
            log.info("Delete comment: {}", commentId);
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Comment not found by id: " + commentId);
        }
    }

    @Override
    public void deleteComment(Long commentId) {
        if (commentRepository.existsById(commentId)) {
            log.info("Admin delete comment: {}", commentId);
            commentRepository.deleteById(commentId);
        } else {
            throw new NotFoundException("Comment not found by id: " + commentId);
        }
    }

    @Override
    public CommentDto getComment(Long userId, Long commentId) {
        getUser(userId);
        log.info("Get comment by id: {}", commentId);
        return CommentMapper.INSTANCE.toDto(getCommentById(commentId));
    }

    private Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Не найден комментарий с id: " + commentId));
    }

    @Override
    public CommentDto createReply(Long userId, Long parentCommentId, CommentDto commentDto) {
        getUser(userId);
        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundException("Parent comment not found: " + parentCommentId));

        Comment comment = CommentMapper.INSTANCE.toEntity(commentDto);
        comment.setUser(userId);
        comment.setEvent(parentComment.getEvent());
        comment.setParentComment(parentComment);

        log.info("Create reply to comment: {}", comment);

        CommentDto dto = CommentMapper.INSTANCE.toDto(commentRepository.save(comment));
        dto.setUser(userClient.getUser(userId).getBody());

        return dto;
    }

    @Override
    public List<CommentDto> getReplies(Long commentId) {
        log.info("Get replies for commentId: {}", commentId);
        return CommentMapper.INSTANCE.toDtos(commentRepository.findAllByParentCommentId(commentId));
    }

    private UserShortDto getUser(Long userId) {
        UserShortDto user = userClient.getUser(userId).getBody();
        if (user == null) {
            throw new NotFoundException("Не найден пользователь с id: " + userId);
        }
        return user;
    }
}