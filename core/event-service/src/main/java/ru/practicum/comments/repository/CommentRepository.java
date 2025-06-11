package ru.practicum.comments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.comments.model.Comment;
import ru.practicum.dto.enums.CommentStatus;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus commentStatus);

    List<Comment> findAllByEventIdInAndStatus(List<Long> idsList, CommentStatus commentStatus);

    List<Comment> findAllByStatus(CommentStatus commentStatus);
}
