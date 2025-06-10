package ru.yandex.practicum.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;
import ru.yandex.practicum.kafka.KafkaClient;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionHandlerImpl implements UserActionHandler {
    @Value(value = "${userActionTopic}")
    private String topic;
    private final KafkaClient kafkaClient;


    @Override
    public void handle(UserActionProto userAction) {
        UserActionAvro eventAvro = map(userAction);
        ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(topic, null,
                eventAvro.getTimestamp().getEpochSecond(), null, eventAvro);
        kafkaClient.getProducer().send(producerRecord);
        log.info("Action from user ID = {} send to topic: {}", userAction.getUserId(), topic);
    }

    private UserActionAvro map(UserActionProto userAction) {
        return UserActionAvro.newBuilder()
                .setUserId(userAction.getUserId())
                .setEventId(userAction.getEventId())
                .setActionType(mapToAvro(userAction.getActionType()))
                .setTimestamp(mapToInstant(userAction.getTimestamp()))
                .build();
    }

    private Instant mapToInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private ActionTypeAvro mapToAvro(ActionTypeProto actionTypeProto) {
        switch (actionTypeProto) {
            case ACTION_VIEW -> {
                return ActionTypeAvro.VIEW;
            }
            case ACTION_REGISTER -> {
                return ActionTypeAvro.REGISTER;
            }
            case ACTION_LIKE -> {
                return ActionTypeAvro.LIKE;
            }
            default -> {
                return null;
            }
        }
    }
}
