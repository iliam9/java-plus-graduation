package ru.yandex.practicum.handler;

import com.google.common.base.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.comparator.UserActionTimestampComparator;
import ru.yandex.practicum.dto.EventSimilarityCoef;
import ru.yandex.practicum.dto.RecommendedEvent;
import ru.yandex.practicum.mapper.AnalyzerMapper;
import ru.yandex.practicum.model.UserAction;
import ru.yandex.practicum.repository.EventSimilarityRepository;
import ru.yandex.practicum.repository.UserActionRepository;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationHandler {
    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;
    private final AnalyzerMapper mapper;

    public Stream<RecommendedEventProto> handle(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        int limit = request.getMaxResults();
        Supplier<Stream<UserAction>> userActionStreamSupplier = () -> userActionRepository.findByUserId(userId);
        if (userActionStreamSupplier.get().findAny().isEmpty()) {
            return Stream.empty();
        }
        List<Long> latestEventWithInteractionIds = getLatestEvent(userActionStreamSupplier, limit);
        List<Long> similarEvent = getSimilarEvent(latestEventWithInteractionIds, limit);
        if (similarEvent.isEmpty()) {
            return Stream.empty();
        }
        Map<Long, Double> ratingEventWithInteraction = getMaxUserWeight(userActionStreamSupplier);
        Supplier<Stream<EventSimilarityCoef>> eventStreamSupplier = () -> eventSimilarityRepository
                .findSimilarity(similarEvent, ratingEventWithInteraction.keySet());
        Map<Long, Double> eventWeightedRatingSums =
                getWeightedRatingSum(eventStreamSupplier, ratingEventWithInteraction);
        Map<Long, Double> eventCoefSums = getEventCoefSums(eventStreamSupplier);
        return getRecommendedEvent(similarEvent, eventWeightedRatingSums, eventCoefSums);
    }

    public Stream<RecommendedEventProto> handle(SimilarEventsRequestProto request) {
        long eventId = request.getEventId();
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();
        List<Long> eventWithInteraction = userActionRepository.findByUserId(userId)
                .map(UserAction::getEventId)
                .distinct()
                .toList();
        return eventSimilarityRepository.findSimilarEvent(List.of(eventId), Limit.of(maxResults))
                .filter(event -> !eventWithInteraction.contains(event.getEventId()))
                .sorted(RecommendedEvent::compareTo)
                .map(mapper::map);

    }

    public Stream<RecommendedEventProto> handle(InteractionsCountRequestProto request) {
        List<Long> eventIdList = request.getEventIdList();
        return userActionRepository.getRating(eventIdList).map(mapper::map);
    }

    private Stream<RecommendedEventProto> getRecommendedEvent(List<Long> similarEvent,
                                                              Map<Long, Double> eventWeightedRatingSums,
                                                              Map<Long, Double> eventCoefSums) {
        return similarEvent.stream()
                .map(event -> new RecommendedEvent(
                        event, eventWeightedRatingSums.get(event) / eventCoefSums.get(event))
                )
                .map(mapper::map);
    }

    private static Map<Long, Double> getEventCoefSums(Supplier<Stream<EventSimilarityCoef>> eventStreamSupplier) {
        return eventStreamSupplier.get()
                .collect(Collectors.groupingBy(EventSimilarityCoef::getEventA,
                        Collectors.summingDouble(EventSimilarityCoef::getScore)));
    }

    private Map<Long, Double> getWeightedRatingSum(Supplier<Stream<EventSimilarityCoef>> eventStreamSupplier,
                                                   Map<Long, Double> ratingEventWithInteraction) {
        return eventStreamSupplier.get()
                .peek(e -> e.setScore(
                        e.getScore() * ratingEventWithInteraction.getOrDefault(e.getEventB(), 0.0))
                )
                .collect(Collectors.groupingBy(EventSimilarityCoef::getEventA,
                        Collectors.summingDouble(EventSimilarityCoef::getScore)));
    }

    private void removeEventWithInteraction(Supplier<Stream<UserAction>> userActionStreamSupplier,
                                            List<Long> similarEvent) {
        List<Long> eventList = userActionStreamSupplier.get()
                .map(UserAction::getEventId)
                .distinct().toList();
        try {
            similarEvent.removeFirst();
        } catch (Exception e) {
            log.error("{}", e);
        }
    }

    private List<Long> getSimilarEvent(List<Long> latestEventWithInteractionIds, int limit) {
        return eventSimilarityRepository
                .findSimilarEvent(latestEventWithInteractionIds, Limit.of(limit))
                .map(RecommendedEvent::getEventId)
                .filter(event -> !latestEventWithInteractionIds.contains(event))
                .distinct()
                .toList();
    }

    private List<Long> getLatestEvent(Supplier<Stream<UserAction>> userActionStreamSupplier, int limit) {
        return userActionStreamSupplier.get()
                .sorted(new UserActionTimestampComparator())
                .limit(limit)
                .map(UserAction::getEventId)
                .distinct()
                .toList();
    }

    private Map<Long, Double> getMaxUserWeight(Supplier<Stream<UserAction>> userActionStreamSupplier) {
        return userActionStreamSupplier.get()
                .map(e -> new RecommendedEvent(e.getEventId(), e.getWeight()))
                .collect(Collectors.groupingBy(RecommendedEvent::getEventId,
                        Collectors.maxBy(Comparator.comparing(RecommendedEvent::getScore))))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> e.getValue().get().getScore()));
    }

}
