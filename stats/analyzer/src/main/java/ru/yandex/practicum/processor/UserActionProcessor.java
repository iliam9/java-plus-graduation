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
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.yandex.practicum.AnalyzerConfig;
import ru.yandex.practicum.handler.UserActionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionProcessor implements Runnable {
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
    private final KafkaConsumer<String, UserActionAvro> consumer;
    private final UserActionHandler handler;
    private final AnalyzerConfig config;

    @Override
    public void run() {
        try {
            consumer.subscribe(config.getActionTopics());
            while (true) {
                ConsumerRecords<String, UserActionAvro> records =
                        consumer.poll(config.getActionConsumeAttemptTimeout());
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    UserActionAvro userAction = record.value();
                    log.info("Received action from user ID = {}", userAction.getUserId());
                    handler.handle(userAction);
                    manageOffsets(record, consumer);
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

    private static void manageOffsets(ConsumerRecord<String, UserActionAvro> record,
                                      KafkaConsumer<String, UserActionAvro> consumer) {
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
    }
}
