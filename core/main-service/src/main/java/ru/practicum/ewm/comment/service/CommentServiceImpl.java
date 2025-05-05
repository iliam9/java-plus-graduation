package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentRequest;
import ru.practicum.ewm.comment.dto.params.CommentParams;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.errorHandler.exception.AccessForbiddenException;
import ru.practicum.ewm.errorHandler.exception.ConflictDataException;
import ru.practicum.ewm.errorHandler.exception.NotFoundException;
import ru.practicum.ewm.event.dto.ParamEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(ParamEventDto params, NewCommentRequest request) {
        User author = getUser(params.getUserId());
        Event event = getEvent(params.getEventId());
        Comment comment = commentMapper.mapToComment(request, author, event);
        comment = commentRepository.save(comment);
        log.info("Comment {} added to event {}", comment.getText(), event.getTitle());
        return commentMapper.mapToCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(CommentParams params) {
        User author = getUser(params.getUserId());
        Comment comment = getComment(params.getCommentId());
        validateCommentAuthor(author, comment);
        commentRepository.delete(comment);
        log.info("Comment {} was deleted  by user {}",
                comment.getText(), author.getName());
    }

    @Override
    public List<CommentDto> findEventComment(ParamEventDto params) {
        Event event = getUserEvent(params);
        List<Comment> comments = commentRepository.findAllByEvent(event);
        return commentMapper.mapToCommentDto(comments);
    }

    @Override
    public List<CommentDto> findUserComments(long userId) {
        User author = getUser(userId);
        List<Comment> comments = commentRepository.findAllByAuthor(author);
        return commentMapper.mapToCommentDto(comments);
    }

    @Override
    public CommentDto updateComment(CommentParams params, NewCommentRequest request) {
        User author = getUser(params.getUserId());
        Comment comment = getComment(params.getCommentId());
        validateCommentAuthor(author, comment);
        validateCommentDate(comment);
        commentMapper.update(comment, request);
        commentRepository.save(comment);
        log.info("Comment ID = {} updated", comment.getId());
        return commentMapper.mapToCommentDto(comment);
    }

    private void validateCommentAuthor(User author, Comment comment) {
        if (!author.getId().equals(comment.getAuthor().getId())) {
            log.error("User with ID = {} has no rights to delete comment ID = {}", author.getId(), comment.getId());
            throw new AccessForbiddenException("No rights to delete comment");
        }

    }

    private void validateCommentDate(Comment comment) {
        if (comment.getCreated().plusHours(1).isBefore(LocalDateTime.now())) {
            log.error("Comment ID = {} is not available for change now", comment.getId());
            throw new ConflictDataException(
                    String.format("Comment ID = %d is not available for change now", comment.getId()));
        }

    }

    private Comment getComment(long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Not found comment with ID = {}", commentId);
                    return new NotFoundException(String.format("Not found comment with ID = %d", commentId));
                });
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Not found user with ID = {}", userId);
                    return new NotFoundException(String.format("Not found user with ID = %d", userId));
                });
    }

    private Event getEvent(long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Not found event with ID = {}", eventId);
                    return new NotFoundException(String.format("Not found event with ID = %d", eventId));
                });
    }

    private Event getUserEvent(ParamEventDto paramEventDto) {
        long userId = paramEventDto.getUserId();
        long eventId = paramEventDto.getEventId();
        User user = getUser(userId);
        Event event = getEvent(eventId);
        if (event.getInitiator() != user) {
            log.error("Event with ID = {} is not found", eventId);
            throw new NotFoundException(
                    String.format("Not found event with ID = %d", eventId));
        }
        return event;
    }

}
