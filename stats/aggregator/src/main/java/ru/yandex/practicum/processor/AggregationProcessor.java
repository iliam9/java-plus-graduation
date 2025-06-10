package ru.yandex.practicum.processor;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.AggregatorConfig;
import ru.yandex.practicum.handler.RecordHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationProcessor {
    private static final int COUNT_COMMIT_OFFSETS = 10;
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    private final RecordHandler recordHandler;
    private final AggregatorConfig aggregatorConfig;
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final KafkaProducer<String, SpecificRecordBase> producer;


    public void start() {
        try {
            consumer.subscribe(aggregatorConfig.getSensorTopic());

            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer
                        .poll(aggregatorConfig.getConsumeAttemptTimeout());

                int count = 0;
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    Stream<EventSimilarityAvro> eventSimilarityAvroStream = recordHandler.handle(record.value());
                    eventSimilarityAvroStream.forEach(this::sendToTopic);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }

        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Error sensor events processing:", e);
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                consumer.close();
                log.info("Consumer closed");
                producer.close();
                log.info("Producer closed");
            }
        }
    }

    public void stop() {
        consumer.wakeup();
    }

    private void sendToTopic(EventSimilarityAvro eventSimilarityAvro) {
        ProducerRecord<String, SpecificRecordBase> producerRecord =
                new ProducerRecord<>(aggregatorConfig.getSimilarityTopic(),
                        null,
                        eventSimilarityAvro.getTimestamp().getEpochSecond(),
                        null,
                        eventSimilarityAvro);
        producer.send(producerRecord);
        log.info("Event similarity for events A = {} and B = {}: {} send to topic: {}",
                eventSimilarityAvro.getEventA(),
                eventSimilarityAvro.getEventB(),
                eventSimilarityAvro.getScore(),
                aggregatorConfig.getSimilarityTopic());
    }

    private static void manageOffsets(ConsumerRecord<String, UserActionAvro> record, int count,
                                      KafkaConsumer<String, UserActionAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % COUNT_COMMIT_OFFSETS == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Commit offsets error: {}", offsets, exception);
                }
            });
        }
    }
}