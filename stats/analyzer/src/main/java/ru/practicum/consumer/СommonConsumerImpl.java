package ru.practicum.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.handler.EventsSimilarityHandler;
import ru.practicum.handler.UserActionHandler;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class СommonConsumerImpl implements CommonConsumer {

    @Value("${analyzer.kafka.event-similarity-consumer.topic}")
    private String eventSimilarityTopic;

    @Value("${analyzer.kafka.user-actions-consumer.topic}")
    private String userActionTopic;

    private final KafkaConsumer<String, EventSimilarityAvro> eventSimilarityConsumer;
    private final KafkaConsumer<String, UserActionAvro> userActionConsumer;

    private static final Map<TopicPartition, OffsetAndMetadata> currentEventsSimilaritiesOffsets = new HashMap<>();
    private static final Map<TopicPartition, OffsetAndMetadata> currentUserActionOffsets = new HashMap<>();

    private final EventsSimilarityHandler eventsSimilarityHandler;
    private final UserActionHandler userActionHandler;


    @Override
    public void start() {
        try {
            eventSimilarityConsumer.subscribe(List.of(eventSimilarityTopic));
            Runtime.getRuntime().addShutdownHook(new Thread(eventSimilarityConsumer::wakeup));
            log.info("Анализатор подписался на топик " + eventSimilarityTopic);

            userActionConsumer.subscribe(List.of(userActionTopic));
            Runtime.getRuntime().addShutdownHook(new Thread(userActionConsumer::wakeup));
            log.info("Анализатор подписался на топик " + userActionTopic);

            while (true) {
                int eventSimilarityCount = 0;
                ConsumerRecords<String, EventSimilarityAvro> eventSimilarityrecords = eventSimilarityConsumer.poll(Duration.ofMillis(1000));
                log.info("Получены " + eventSimilarityrecords.count() + " записей о сходстве двух мероприятий из топика " + eventSimilarityTopic);
                for (ConsumerRecord<String, EventSimilarityAvro> record : eventSimilarityrecords) {
                    eventsSimilarityHandler.handleEventsSimilarity(record.value());
                    manageOffsetsForEventsSimilarity(record, eventSimilarityCount, eventSimilarityConsumer);
                    eventSimilarityCount++;
                }
                eventSimilarityConsumer.commitAsync();

                int userActionCount = 0;
                ConsumerRecords<String, UserActionAvro> userActionRecords = userActionConsumer.poll(Duration.ofMillis(1000));
                log.info("Получены " + userActionRecords.count() + " записей о действиях пользователей из топика " + userActionTopic);
                for (ConsumerRecord<String, UserActionAvro> record : userActionRecords) {
                    userActionHandler.handleUserAction(record.value());
                    manageOffsetsForUserAction(record, userActionCount, userActionConsumer);
                    userActionCount++;
                }
                userActionConsumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер в блоке finally
        } catch (Exception e) {
            log.error("Произошла ошибка при прочтении сообщения из кафки. \n {} : \n {}", e.getMessage(),
                    e.getStackTrace());
        } finally {
            try {
                eventSimilarityConsumer.commitSync();
                userActionConsumer.commitSync();
            } finally {
                eventSimilarityConsumer.close();
                userActionConsumer.close();
            }
        }
    }

    private static void manageOffsetsForEventsSimilarity(ConsumerRecord<String, EventSimilarityAvro> record, int count, KafkaConsumer<String, EventSimilarityAvro> consumer) {
        currentEventsSimilaritiesOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentEventsSimilaritiesOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private static void manageOffsetsForUserAction(ConsumerRecord<String, UserActionAvro> record, int count, KafkaConsumer<String, UserActionAvro> consumer) {
        currentUserActionOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );

        if (count % 10 == 0) {
            consumer.commitAsync(currentUserActionOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

}
