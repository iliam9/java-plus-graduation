package ru.practicum.client;

import com.google.protobuf.Timestamp;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.grpc.collector.UserActionControllerGrpc;
import ru.practicum.grpc.messages.ActionTypeProto;
import ru.practicum.grpc.messages.UserActionProto;

import java.time.Instant;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub stub;


    private void sendUserAction(Long userId, Long eventId, ActionTypeProto actionType) {
        UserActionProto userAction = UserActionProto.newBuilder()
                .setUserId(userId)
                .setEventId(eventId)
                .setActionType(actionType)
                .setTimestamp(Timestamp.newBuilder()
                        .setSeconds(Instant.now().getEpochSecond())
                        .setNanos(Instant.now().getNano())
                        .build())
                .build();
        stub.collectUserAction(userAction);
    }

    /**
     * Отправить в Kafka сообщение о просмотре мероприятия пользователем.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор мероприятия
     */
    public void sendView(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_VIEW);
    }

    /**
     * Отправить в Kafka сообщение о регистрации пользователя на участие в мероприятии.
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор мероприятия
     */
    public void sendRegistration(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_REGISTER);
    }

    /**
     * Отправить в Kafka сообщение о реакции пользователя на мероприятие (установка лайка).
     *
     * @param userId  идентификатор пользователя
     * @param eventId идентификатор мероприятия
     */
    public void sendLike(Long userId, Long eventId) {
        sendUserAction(userId, eventId, ActionTypeProto.ACTION_LIKE);
    }

}
