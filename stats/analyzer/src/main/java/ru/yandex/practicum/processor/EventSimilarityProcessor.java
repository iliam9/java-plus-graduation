package ru.yandex.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.yandex.practicum.AnalyzerConfig;
import ru.yandex.practicum.handler.EventSimilarityHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSimilarityProcessor implements Runnable {
    private static final int COUNT_COMMIT_OFFSETS = 10;
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, EventSimilarityAvro> consumer;
    private final EventSimilarityHandler handler;
    private final AnalyzerConfig config;

    @Override
    public void run() {
        try {
            consumer.subscribe(config.getSimilarityTopics());

            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer
                        .poll(config.getSimilarityConsumeAttemptTimeout());
                int count = 0;
                for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
                    EventSimilarityAvro eventSimilarityAvro = record.value();
                    log.info("Received similarity for events A = {} and B = {}",
                            eventSimilarityAvro.getEventA(),
                            eventSimilarityAvro.getEventB());
                    handler.handle(eventSimilarityAvro);
                    manageOffsets(record,count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {

        } catch (Exception e) {
            log.error("Error:", e);
        } finally {
            try {
                consumer.commitSync(currentOffsets);
            } finally {
                consumer.close();
                log.info("Consumer close");
            }
        }
    }

    public void stop() {
        consumer.wakeup();
    }

    private static void manageOffsets(ConsumerRecord<String, EventSimilarityAvro> record, int count,
                                      KafkaConsumer<String, EventSimilarityAvro> consumer) {
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
