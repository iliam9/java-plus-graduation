package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.model.Hit;
import ru.practicum.ewm.stats.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {
    @Query("select new ru.practicum.ewm.stats.model.Stat( " +
            " h.app, h.uri, case when :unique = TRUE then count(distinct h.ip) else count(h.ip) end) " +
            " from Hit h" +
            " where h.time between :start and :end " +
            "   and (h.uri in :uris or :uris is null) " +
            " group by h.app, h.uri " +
            " order by case when :unique = TRUE then count(distinct h.ip) else count(h.ip) end desc")
    List<Stat> getStat(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") List<String> uris,
            @Param("unique") boolean unique
    );
}
