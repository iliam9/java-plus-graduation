package ru.practicum.service;

import io.grpc.stub.StreamObserver;
import ru.practicum.grpc.messages.InteractionsCountRequestProto;
import ru.practicum.grpc.messages.RecommendedEventProto;
import ru.practicum.grpc.messages.SimilarEventsRequestProto;
import ru.practicum.grpc.messages.UserPredictionsRequestProto;

public interface RecommendationService {

    void getRecommendationsForUser(UserPredictionsRequestProto userPredictionsRequest,
                                   StreamObserver<RecommendedEventProto> responseObserver);

    void getSimilarEvents(SimilarEventsRequestProto similarEventsRequest,
                          StreamObserver<RecommendedEventProto> responseObserver);

    void getInteractionsCount(InteractionsCountRequestProto interactionsCountRequest,
                              StreamObserver<RecommendedEventProto> responseObserver);

}
