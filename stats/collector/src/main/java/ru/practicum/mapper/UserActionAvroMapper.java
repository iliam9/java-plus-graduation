package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.messages.ActionTypeProto;
import ru.practicum.grpc.messages.UserActionProto;

import java.time.Instant;

@Component
public class UserActionAvroMapper {

    public UserActionAvro userActionToAvro(UserActionProto userActionProto) {
        return UserActionAvro.newBuilder()
                .setUserId(userActionProto.getUserId())
                .setEventId(userActionProto.getEventId())
                .setActionType(actionTypeProtoToAvro(userActionProto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(userActionProto.getTimestamp().getSeconds(),
                        userActionProto.getTimestamp().getNanos()))
                .build();
    }

    private ActionTypeAvro actionTypeProtoToAvro(ActionTypeProto actionTypeProto) {
        switch (actionTypeProto) {
            case ACTION_VIEW:
                return ActionTypeAvro.VIEW;
            case ACTION_REGISTER:
                return ActionTypeAvro.REGISTER;
            case ACTION_LIKE:
                return ActionTypeAvro.LIKE;
            default:
                throw new IllegalArgumentException("В protobuf-сущности передан неизвестный тип действия: " +
                        actionTypeProto);
        }
    }

}
