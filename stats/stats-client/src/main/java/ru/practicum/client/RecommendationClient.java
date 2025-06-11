package ru.practicum.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.messages.InteractionsCountRequestProto;
import ru.practicum.grpc.messages.RecommendedEventProto;
import ru.practicum.grpc.messages.SimilarEventsRequestProto;
import ru.practicum.grpc.messages.UserPredictionsRequestProto;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class RecommendationClient {

    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub stub;

    /**
     * Получить поток рекомендованных мероприятий для указанного пользователя.
     *
     * @param userId     идентификатор пользователя
     * @param maxResults ограничение количества мероприятий в результате выполнения запроса
     */
    public Stream<RecommendedEventProto> getRecommendationsForUser(Long userId, Integer maxResults) {
        UserPredictionsRequestProto userPredictionsRequest = UserPredictionsRequestProto.newBuilder()
                .setUserId(Math.toIntExact(userId))
                .setMaxResults(maxResults)
                .build();
        Iterator<RecommendedEventProto> iterator = stub.getRecommendationsForUser(userPredictionsRequest);
        return asStream(iterator);
    }

    /**
     * Получить поток мероприятий, с которыми не взаимодействовал данный пользователь,
     * но которые максимально похожи на указанное мероприятие.
     *
     * @param eventId    идентификатор мероприятия с которым сравниваем
     * @param userId     идентификатор пользователя
     * @param maxResults ограничение количества мероприятий в результате выполнения запроса
     */
    public Stream<RecommendedEventProto> getSimilarEvents(Long eventId, Long userId, Integer maxResults) {
        SimilarEventsRequestProto similarEventsRequest = SimilarEventsRequestProto.newBuilder()
                .setEventId(Math.toIntExact(eventId))
                .setUserId(Math.toIntExact(userId))
                .setMaxResults(maxResults)
                .build();
        Iterator<RecommendedEventProto> iterator = stub.getSimilarEvents(similarEventsRequest);
        return asStream(iterator);
    }

    /**
     * Получить поток мероприятий с суммой максимальных весов действий каждого пользователя для каждого из этих мероприятий.
     *
     * @param eventIds список идентификаторов мероприятий
     */
    public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto interactionsCountRequest = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        Iterator<RecommendedEventProto> iterator = stub.getInteractionsCount(interactionsCountRequest);
        return asStream(iterator);
    }


    private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                false
        );
    }

}
