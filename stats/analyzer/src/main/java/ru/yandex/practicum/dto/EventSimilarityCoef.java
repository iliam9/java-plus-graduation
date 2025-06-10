package ru.yandex.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventSimilarityCoef {
    private long eventA;
    private long eventB;
    private double score;
}
