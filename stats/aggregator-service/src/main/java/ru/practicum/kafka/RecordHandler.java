package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordHandler {
    private static final double VIEW = 0.4;
    private static final double LIKE = 1;
    private static final double REGISTER = 0.8;

    @Value("${event.topic}")
    private String topic;

    private Map<Long, Map<Long, Double>> maxUserAction = new HashMap<>();
    private Map<Long, Double> totalSum = new HashMap<>();
    private Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    List<EventSimilarityAvro> updateState(UserActionAvro userAction) {
        log.info("UserAction state is {}", userAction);

        long userId = userAction.getUserId();
        long eventId = userAction.getEventId();

        Double scopeActionType = getActionTypeScope(userAction.getActionType());

        if (maxUserAction.containsKey(eventId)) {
            Map<Long, Double> userActions = maxUserAction.get(eventId);
            if (userActions.containsKey(userId)) {
                var scope = userActions.get(userId);
                if (scope < scopeActionType) {
                    userActions.put(userId, scopeActionType);
                    calculateTotalScope(eventId, scopeActionType - scope); //Если были изменения пересчитываем сумму
                    calculateMinWeightsSums(eventId, userId,  scopeActionType);
                } else {
                    return new ArrayList<>();
                }
            } else {
                userActions.put(userId, scopeActionType);
                totalSum.put(eventId, totalSum.get(eventId) + scopeActionType);
                calculateMinWeightsSumsFirsTime(eventId); //Считаем сумму минимальных весов для каждой пары мероприятий
            }
        } else {
            maxUserAction.computeIfAbsent(eventId, k -> new HashMap<>()).put(userId, scopeActionType);
            totalSum.put(eventId, totalSum.getOrDefault(eventId, 0.0) + scopeActionType);
            calculateMinWeightsSumsFirsTime(eventId); //Считаем сумму минимальных весов для каждой пары мероприятий
        }

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Double> entry : minWeightsSums.get(eventId).entrySet()) {
            similarities.add(EventSimilarityAvro.newBuilder()
                    .setEventA(eventId)
                    .setEventB(entry.getKey())
                    .setScore(similarity(eventId, entry.getKey()))
                    .setTimestamp(userAction.getTimestamp())
                    .build());
        }

        return similarities;
    }

    private Double getActionTypeScope(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> VIEW;
            case LIKE -> LIKE;
            case REGISTER -> REGISTER;
        };
    }

    private void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void calculateTotalScope(long eventId, double delta) {
        totalSum.put(eventId, totalSum.get(eventId) + delta);
    }

    private void calculateMinWeightsSumsFirsTime(long eventId) {
        minWeightsSums.put(eventId, new HashMap<>());
        Map<Long, Double> userActions1 = maxUserAction.get(eventId);
        for (long event : maxUserAction.keySet()) {
            if (eventId != event) {
                Map<Long, Double> userActions2 = maxUserAction.get(event);
                double sum = 0.0;

                for (Map.Entry<Long, Double> entry : userActions1.entrySet()) {
                    long userId = entry.getKey();
                    double weight1 = entry.getValue();
                    double weight2 = userActions2.getOrDefault(userId, 0.0);
                    sum += Math.min(weight1, weight2);
                }
                put(eventId, event, sum);
            }
        }
    }

    private void calculateMinWeightsSums(long eventId, long userId, double newWeight) {
        double oldA = maxUserAction.get(eventId).get(userId);
        for (long event: minWeightsSums.keySet()) {
            if (eventId != event) {
                double oldB = maxUserAction.get(event).getOrDefault(userId, 0.0);
                double delta = Math.min(oldA, oldB) - Math.min(oldB, newWeight);
                put(event, eventId, get(event, eventId) + delta);
            }
        }
    }

    private double similarity(long eventA, long eventB) {
        double sumA = totalSum.get(eventA);
        double sumB = totalSum.get(eventB);

        double minSym = minWeightsSums.get(eventA).get(eventB);

        return minSym / (Math.sqrt(sumA) * Math.sqrt(sumB));
    }
}