package ru.practicum.controller;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.grpc.analyzer.RecommendationsControllerGrpc;
import ru.practicum.grpc.messages.InteractionsCountRequestProto;
import ru.practicum.grpc.messages.RecommendedEventProto;
import ru.practicum.grpc.messages.SimilarEventsRequestProto;
import ru.practicum.grpc.messages.UserPredictionsRequestProto;
import ru.practicum.service.RecommendationService;

@GrpcService
@RequiredArgsConstructor
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto userPredictionsRequest,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        recommendationService.getRecommendationsForUser(userPredictionsRequest, responseObserver);
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto similarEventsRequest,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        recommendationService.getSimilarEvents(similarEventsRequest, responseObserver);
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto interactionsCountRequest,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        recommendationService.getInteractionsCount(interactionsCountRequest, responseObserver);
    }

}
