package ru.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.messages.UserActionProto;
import ru.practicum.mapper.UserActionAvroMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionHandler {

    @Value("${collector.kafka.topic}")
    private String userActionTopic;

    private final Producer<String, SpecificRecordBase> producer;

    public final UserActionAvroMapper userActionAvroMapper;

    public void handle(UserActionProto userActionProto) {
        UserActionAvro userActionAvro = userActionAvroMapper.userActionToAvro(userActionProto);
        ProducerRecord<String, SpecificRecordBase> producerUserActionRecord = new ProducerRecord<>(
                userActionTopic,
                userActionAvro);
        Future<RecordMetadata> message = producer.send(producerUserActionRecord);
        try {
            message.get();
            log.info("Пользовательское действие типа {} успешно отправлено в топик {}", userActionAvro.getActionType().toString(), userActionTopic);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Ошибка во время отправки сообщения в Kafka");
        }
    }

}
