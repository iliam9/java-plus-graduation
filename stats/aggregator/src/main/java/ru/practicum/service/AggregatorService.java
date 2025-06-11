package ru.practicum.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.configuration.ScoreSettings;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregatorService {

    private final ScoreSettings scoreSettings;

    //Ключ — идентификатор мероприятия; значение — мапа, где:
    //ключ — идентификатор пользователя, а значение — максимальный вес из всех действий данного пользователя с этим мероприятием.
    private final Map<Long, Map<Long, Double>> userEventWeights = new HashMap<>();

    //Ключ — идентификатор мериприятия; значение — сумма весов действий пользователей с этим мериприятием.
    private final Map<Long, Double> eventWeightSums = new HashMap<>();

    //Ключ — идентификатор первого мероприятия; значение — мапа, где:
    //ключ — идентификатор второго мероприятия, а значение — сумма минимальных весов данных мероприятий.
    private final Map<Long, Map<Long, Double>> minWeightsSum = new HashMap<>();


    public List<EventSimilarityAvro> createEventSimilarityMessages(UserActionAvro userAction) {

        List<EventSimilarityAvro> result = new ArrayList<>();

        Long userId = userAction.getUserId();
        Long eventId = userAction.getEventId();
        Double newWeight = getScoreByActionType(userAction.getActionType());

        // Карта весов по пользователям для конкретного мероприятия
        Map<Long, Double> weightsByUsers = userEventWeights.computeIfAbsent(eventId, e -> new HashMap<>());
        // Вес данного мероприятия для пользователя
        Double existedWeight = weightsByUsers.getOrDefault(userId, 0.0);

        if (newWeight > existedWeight) {
            weightsByUsers.put(userId, newWeight);
            userEventWeights.put(eventId, weightsByUsers);

            Double totalWeight = calculateAndUpdateTotalWeight(eventId, existedWeight, newWeight);

            for (Long anotherEventId : userEventWeights.keySet()) {
                if (Objects.equals(anotherEventId, eventId) || !userEventWeights.get(anotherEventId).containsKey(userId)) {
                    continue;
                }

                Double anotherEventWeight = userEventWeights.get(anotherEventId).get(userId);
                Double weightDifference = Math.min(newWeight, anotherEventWeight) - Math.min(existedWeight, anotherEventWeight);
                Double existedSumMinWeights = getSumMinWeights(anotherEventId, eventId);
                Double sumMinWeights = existedSumMinWeights;

                if (weightDifference != 0) {
                    Double newSumMinWeights = existedSumMinWeights + weightDifference;
                    sumMinWeights = newSumMinWeights;
                    putSumMinWeights(anotherEventId, eventId, newSumMinWeights);
                }

                Double anotherEventSumMinWeights = eventWeightSums.getOrDefault(anotherEventId, 0.0);

                Double similarity = calculateSimilarity(sumMinWeights, totalWeight, anotherEventSumMinWeights);

                EventSimilarityAvro eventSimilarity = EventSimilarityAvro.newBuilder()
                        .setEventA(Math.min(eventId, anotherEventId))
                        .setEventB(Math.max(eventId, anotherEventId))
                        .setScore(similarity)
                        .setTimestamp(userAction.getTimestamp())
                        .build();
                result.add(eventSimilarity);
            }
        }

        return result;

    }


    private Double getScoreByActionType(ActionTypeAvro actionType) {

        Double score = 0d;
        switch (actionType) {
            case VIEW:
                score = scoreSettings.getView();
                break;
            case REGISTER:
                score = scoreSettings.getRegistration();
                break;
            case LIKE:
                score = scoreSettings.getLike();
                break;
            default:
                throw new ValidationException("Передан некорректный тип действия пользователя.");
        }
        return score;

    }

    private Double calculateAndUpdateTotalWeight(Long eventId, Double existedWeight, Double newWeight) {
        Double existedTotalWeight = eventWeightSums.getOrDefault(eventId, 0.0);
        Double newTotalWeight = existedTotalWeight + (newWeight - existedWeight);
        eventWeightSums.put(eventId, newTotalWeight);
        return newTotalWeight;
    }

    private void putSumMinWeights(Long eventBId, Long eventAId, Double sumMinWeights) {
        Long outerKey = Math.min(eventBId, eventAId);
        Long innerKey = Math.max(eventBId, eventAId);
        minWeightsSum.computeIfAbsent(outerKey, e -> new HashMap<>()).put(innerKey, sumMinWeights);
    }

    private Double getSumMinWeights(Long eventBId, Long eventAId) {
        Long outerKey = Math.min(eventBId, eventAId);
        Long innerKey = Math.max(eventBId, eventAId);
        return minWeightsSum.computeIfAbsent(outerKey, e -> new HashMap<>()).getOrDefault(innerKey, 0.0);
    }

    private Double calculateSimilarity(Double minWeightSum, Double totalEventWeight, Double totalOtherEventWeight) {
        return minWeightSum / (Math.sqrt(totalEventWeight) * Math.sqrt(totalOtherEventWeight));
    }

}
