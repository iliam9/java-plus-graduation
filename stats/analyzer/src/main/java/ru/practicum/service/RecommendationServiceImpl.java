package ru.practicum.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.grpc.messages.InteractionsCountRequestProto;
import ru.practicum.grpc.messages.RecommendedEventProto;
import ru.practicum.grpc.messages.SimilarEventsRequestProto;
import ru.practicum.grpc.messages.UserPredictionsRequestProto;
import ru.practicum.handler.UserActionHandler;
import ru.practicum.model.EventsSimilarity;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventsSimilarityRepository;
import ru.practicum.repository.UserActionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final EventsSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;
    private final UserActionHandler userActionHandler;


    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto userPredictionsRequest,
                                          StreamObserver<RecommendedEventProto> responseObserver) {

        Set<UserAction> allUserActions = new HashSet<>(userActionRepository.findByUserId(userPredictionsRequest.getUserId()));

        if (!allUserActions.isEmpty()) {
            Set<Long> freshEventIds = sortAndChooseFreshEventIds(allUserActions, userPredictionsRequest.getMaxResults());
            List<EventsSimilarity> allEventsSimilarities = freshEventIds.stream()
                    .map(eventId -> eventSimilarityRepository.findByEventAIdAndEventBId(eventId, eventId).get()).toList();

            List<EventsSimilarity> notWatchedEventsPair = excludeWatchedEvents(allEventsSimilarities, allUserActions);
            List<EventsSimilarity> moreSimilarEventsPair = sortAndChooseSimilarEventPairs(notWatchedEventsPair, userPredictionsRequest.getMaxResults());

            moreSimilarEventsPair.forEach(s -> {
                long event = allUserActions.contains(s.getEventAId()) ? s.getEventBId() : s.getEventAId();
                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(event)
                        .setScore(s.getSimilarityScore())
                        .build());
            });

            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto similarEventsRequest,
                                 StreamObserver<RecommendedEventProto> responseObserver) {

        List<EventsSimilarity> allEventsSimilarities = eventSimilarityRepository.findByEventAIdOrEventBId(
                similarEventsRequest.getEventId(), similarEventsRequest.getEventId());
        Set<UserAction> allUserActions = new HashSet<>(userActionRepository.findByUserId(similarEventsRequest.getUserId()));

        List<EventsSimilarity> notWatchedEventsPair = excludeWatchedEvents(allEventsSimilarities, allUserActions);
        List<EventsSimilarity> moreSimilarEventsPair = sortAndChooseSimilarEventPairs(notWatchedEventsPair, similarEventsRequest.getMaxResults());

        moreSimilarEventsPair.forEach(s -> {
            long event = s.getEventAId().equals(similarEventsRequest.getEventId()) ? s.getEventBId() : s.getEventAId();
            responseObserver.onNext(RecommendedEventProto.newBuilder()
                    .setEventId(event)
                    .setScore(s.getSimilarityScore())
                    .build());
        });

        responseObserver.onCompleted();

    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto interactionsCountRequest,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        interactionsCountRequest.getEventIdList()
                .forEach(eventId -> {
                    double commonWeight = userActionRepository.findByEventId(eventId)
                            .stream()
                            .mapToDouble((userAction) ->
                                    userActionHandler.getScoreByActionType(userAction.getActionType()))
                            .sum();

                    responseObserver.onNext(RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(commonWeight)
                            .build());
                });

        responseObserver.onCompleted();

    }


    private List<EventsSimilarity> excludeWatchedEvents(List<EventsSimilarity> allEventsSimilarities, Set<UserAction> allUserActions) {
        Set<Long> watchedEventIds = allUserActions.stream().map(UserAction::getEventId).collect(Collectors.toSet());
        return allEventsSimilarities.stream()
                .filter(s -> !watchedEventIds.contains(s.getEventAId()) || !watchedEventIds.contains(s.getEventBId()))
                .toList();
    }

    private Set<Long> sortAndChooseFreshEventIds(Set<UserAction> userActions, Long requestMaxResult) {
        return userActions.stream()
                .sorted(Comparator.comparing(UserAction::getTimestamp).reversed())
                .limit(requestMaxResult)
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());
    }

    private List<EventsSimilarity> sortAndChooseSimilarEventPairs(List<EventsSimilarity> eventsSimilarities, Long requestMaxResult) {
        return eventsSimilarities.stream()
                .sorted(Comparator.comparing(EventsSimilarity::getSimilarityScore).reversed())
                .limit(requestMaxResult)
                .toList();
    }

}
