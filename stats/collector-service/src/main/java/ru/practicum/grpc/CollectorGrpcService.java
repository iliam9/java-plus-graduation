package ru.practicum.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.ActionTypeProto;
import ru.practicum.grpc.stats.UserActionControllerGrpc;
import ru.practicum.grpc.stats.UserActionProto;
import ru.practicum.kafka.KafkaAvroProducer;

import java.time.Instant;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class CollectorGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {

    private final KafkaAvroProducer producer;

    @Value("${user.topic}")
    private String topic;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("collectUserAction: {}", request);

            UserActionAvro avro = mapProtoToAvro(request);
            producer.send(topic, avro);

            responseObserver.onNext(Empty.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    private UserActionAvro mapProtoToAvro(UserActionProto proto) {
        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(mapProtoToAvro(proto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds(), proto.getTimestamp().getNanos()))
                .build();
    }

    private ActionTypeAvro mapProtoToAvro(ActionTypeProto proto) {
        return switch (proto) {
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case ACTION_VIEW ->  ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case UNRECOGNIZED -> null;
        };
    }
}
