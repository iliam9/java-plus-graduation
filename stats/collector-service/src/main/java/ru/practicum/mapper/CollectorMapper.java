package ru.practicum.mapper;

import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.UserActionProto;

import java.time.Instant;

public class CollectorMapper {

    public static UserActionAvro map(UserActionProto proto) {
        return UserActionAvro.newBuilder()
                .setUserId(proto.getUserId())
                .setEventId(proto.getEventId())
                .setActionType(ActionTypeAvro.valueOf(proto.getActionType().name()))
                .setTimestamp(Instant.ofEpochSecond(proto.getTimestamp().getSeconds()))
                .build();
    }
}
