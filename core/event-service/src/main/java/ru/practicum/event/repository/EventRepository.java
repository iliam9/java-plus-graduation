package ru.practicum.event.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.event.model.Event;
import ru.practicum.dto.enums.EventState;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {
    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    List<Event> findAllByInitiatorId(Long initiatorId);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    List<Event> findAllByCategoryId(Long categoryId);

    Optional<Event> findByIdAndState(Long eventId, EventState state);

    List<Event> findAllByIdIn(List<Long> ids);
}
