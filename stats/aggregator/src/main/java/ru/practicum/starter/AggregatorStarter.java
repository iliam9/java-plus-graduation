package ru.practicum.starter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.AggregatorService;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorStarter {

    @Value("${aggregator.kafka.consumer.topic}")
    private String userActionTopic;

    @Value("${aggregator.kafka.producer.topic}")
    private String eventSimilarityTopic;

    private final AggregatorService aggregatorService;
    private final KafkaProducer<String, SpecificRecordBase> producer;
    private final KafkaConsumer<String, SpecificRecordBase> consumer;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();


    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(userActionTopic));
            log.info("Агрегатор подписался на топик " + userActionTopic);

            while (true) {
                ConsumerRecords<String, SpecificRecordBase> records = consumer.poll(Duration.ofMillis(5000));
                log.info("Получены " + records.count() + " записей о действиях пользователей из топика " + userActionTopic);
                int count = 0;

                for (ConsumerRecord<String, SpecificRecordBase> record : records) {
                    UserActionAvro userAction = (UserActionAvro) record.value();
                    //основная логика работы агрегатора
                    List<EventSimilarityAvro> eventSimilarities = aggregatorService.createEventSimilarityMessages(userAction);
                    log.info("Сообщения о сходствах мероприятий сформированы");
                    for (EventSimilarityAvro eventSimilarity : eventSimilarities) {
                        sendToKafka(eventSimilarityTopic, "" + eventSimilarity.getEventA(), eventSimilarity);
                    }

                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Произошла ошибка при формировании сообщения о сходстве двух мероприятий. \n {} : \n {}", e.getMessage(),
                    e.getStackTrace());
        } finally {
            try {
                producer.flush();
                consumer.commitSync();
            } finally {
                consumer.close();
                producer.close();
            }
        }
    }

    public void sendToKafka(String topicName, String eventId, SpecificRecordBase eventSimilarity) {
        ProducerRecord<String, SpecificRecordBase> producerEventSimilarityRecord = new ProducerRecord<>(
                topicName,
                null,
                System.currentTimeMillis(),
                eventId,
                eventSimilarity);
        Future<RecordMetadata> message = producer.send(producerEventSimilarityRecord);
        try {
            message.get();
            log.info("Сообщение о сходстве двух мероприятий отправлено в топик {}", topicName);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Ошибка во время отправки сообщения в Kafka");
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SpecificRecordBase> record, int count,
                                      KafkaConsumer<String, SpecificRecordBase> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

}
