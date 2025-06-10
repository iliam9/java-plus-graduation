package ru.yandex.practicum.mapper;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;

public class WeightMapper {
    private static final double LIKE_WEIGHT = 1.0;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double VIEW_WEIGHT = 0.4;

    static double mapToWeight(ActionTypeAvro typeAvro) {
        switch (typeAvro) {
            case VIEW -> {
                return VIEW_WEIGHT;
            }
            case REGISTER -> {
                return REGISTER_WEIGHT;
            }
            case LIKE -> {
                return LIKE_WEIGHT;
            }
            default -> {
                return 0;
            }
        }
    }
}
