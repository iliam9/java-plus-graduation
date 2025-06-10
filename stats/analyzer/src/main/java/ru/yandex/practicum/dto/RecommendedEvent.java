package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecommendedEvent implements Comparable<RecommendedEvent> {
    private long eventId;
    private double score;

    @Override
    public int compareTo(RecommendedEvent o) {
        return Double.compare(this.score, o.getScore());
    }
}
