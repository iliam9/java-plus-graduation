package ru.practicum.ewm.stats.service;

import ru.practicum.ewm.dto.HitDto;
import ru.practicum.ewm.dto.StatDto;

import java.util.List;

public interface StatsService {
    void addHit(HitDto paramHitDto);

    List<StatDto> getStats(String start, String end, List<String> uris, boolean unique);
}
