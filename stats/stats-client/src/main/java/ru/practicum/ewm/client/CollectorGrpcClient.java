package ru.practicum.ewm.client;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.proto.*;

import java.time.Instant;

@Component
public class CollectorGrpcClient {
    private final UserActionControllerGrpc.UserActionControllerBlockingStub client;

    public CollectorGrpcClient(@GrpcClient("collector-client")
                               UserActionControllerGrpc.UserActionControllerBlockingStub client) {
        this.client = client;
    }

    public void getRecommendationsForUser(long userId, long eventId, ActionTypeProto typeProto) {
        UserActionProto request = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(typeProto)
                .setTimestamp(getTimestamp())
                .build();
        Empty empty = client.collectUserAction(request);
    }

    private Timestamp getTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setNanos(now.getNano())
                .setSeconds(now.getEpochSecond())
                .build();
    }
}
