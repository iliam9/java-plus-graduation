package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.EventsSimilarity;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventsSimilarityRepository extends JpaRepository<EventsSimilarity, Long> {

    Optional<EventsSimilarity> findByEventAIdAndEventBId(Long eventAId, Long eventBId);

    List<EventsSimilarity> findByEventAIdOrEventBId(Long eventAId, Long eventBId);

}
