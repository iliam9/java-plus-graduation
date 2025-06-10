package ru.yandex.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordHandler {
    private static final double LIKE_WEIGHT = 1.0;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double VIEW_WEIGHT = 0.4;

    private final Map<Long, Map<Long, Double>> userWeights;
    private final Map<Long, Double> eventWeightSums;
    private final Map<Long, Map<Long, Double>> minWeightsSums;


    public Stream<EventSimilarityAvro> handle(UserActionAvro userAction) {
        long userId = userAction.getUserId();
        log.info("Received action from user ID = {}", userId);
        long eventA = userAction.getEventId();
        double weight = mapToWeight(userAction.getActionType());
        double oldWeight = getUserWeight(userId, eventA);
        if (oldWeight >= weight) {
            return Stream.empty();
        }
        updateUserWeight(userId, eventA, weight);
        if (oldWeight == 0) {
            addEventWeightSum(eventA, weight);
            addMinWeightSum(userId, weight, eventA);
        } else {
            updateEventWeightSum(eventA, weight, oldWeight);
            updateMinWeightSum(userId, weight, oldWeight, eventA);
        }
        return calculateScore(eventA, userAction.getTimestamp());
    }

    private void updateEventWeightSum(long eventA, double weight, double oldWeight) {
        eventWeightSums.compute(eventA, (k, oldEventWeightSum) -> oldEventWeightSum + weight - oldWeight);
    }

    private void addEventWeightSum(long eventA, double weight) {
        eventWeightSums.put(eventA, weight);
    }

    private Stream<EventSimilarityAvro> calculateScore(long eventA, Instant timestamp) {
        return userWeights.keySet().stream()
                .filter(e -> e != eventA)
                .map(eventB -> calculateScore(eventA, eventB, timestamp));
    }

    private EventSimilarityAvro calculateScore(long eventA, long eventB, Instant timestamp) {
        Double eventAWeightSum = eventWeightSums.get(eventA);
        Double eventBWeightSum = eventWeightSums.get(eventB);
        double score = getMinWeightSum(eventA, eventB) / (Math.sqrt(eventAWeightSum) * Math.sqrt(eventBWeightSum));
        return mapToAvro(eventA, eventB, timestamp, score);
    }

    private void updateMinWeightSum(long userId, double weight, double oldWeight, long eventA) {
        userWeights.entrySet().stream()
                .filter(e -> e.getKey() != eventA)
                .filter(e -> e.getValue().containsKey(userId))
                .forEach(e -> updateMinWeightSum(userId, weight, oldWeight, eventA, e.getKey()));
    }

    private void updateMinWeightSum(long userId, double weightEventA,
                                    double oldWeightEventA, long eventA, long eventB) {
        double deltaMinSum = getDeltaMinSum(userId, weightEventA, oldWeightEventA, eventB);
        double minWeightSum = getMinWeightSum(eventA, eventB);
        putMinWeightSum(eventA, eventB, minWeightSum + deltaMinSum);
    }

    private void addMinWeightSum(long userId, double weight, long eventA) {
        userWeights.entrySet().stream()
                .filter(e -> e.getKey() != eventA)
                .filter(e -> e.getValue().containsKey(userId))
                .forEach(e -> putMinWeightSum(eventA, e.getKey(), weight));
    }

    private double getDeltaMinSum(long userId, double weightEventA, double oldWeightEventA, long eventB) {
        double weightEventB = getUserWeight(userId, eventB);
        return Math.min(weightEventA, weightEventB) - Math.min(oldWeightEventA, weightEventB);
    }

    private double getUserWeight(long userId, long eventId) {
        return userWeights
                .computeIfAbsent(eventId, e -> new HashMap<>())
                .getOrDefault(userId, 0.0);
    }

    public void updateUserWeight(long userId, long eventId, double weight) {
        userWeights
                .get(eventId)
                .put(userId, weight);
    }

    private double mapToWeight(ActionTypeAvro typeAvro) {
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

    public void putMinWeightSum(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    public double getMinWeightSum(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private EventSimilarityAvro mapToAvro(long eventA, long eventB, Instant timestamp, double score) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);
        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setTimestamp(timestamp)
                .setScore(score)
                .build();
    }
}