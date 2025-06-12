package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.EventAction;
import ru.practicum.model.EventActionId;

import java.util.Optional;

public interface EventRepository extends JpaRepository<EventAction, Long> {
    Optional<EventAction> findById(EventActionId id);
}