package ru.practicum.ewm.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    @Query("Select e from Event e where e.initiator = :user order by e.id limit :size offset :from")
    List<Event> findByInitiator(@Param("user") User user,
                                @Param("size") Long size,
                                @Param("from") Long from);

    boolean existsByCategoryId(long catId);

}
