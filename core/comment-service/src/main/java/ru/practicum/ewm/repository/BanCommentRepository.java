package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.model.BanComment;

public interface BanCommentRepository extends JpaRepository<BanComment, Long> {

    BanComment findByUserIdAndEventId(Long userId, Long eventId);
}