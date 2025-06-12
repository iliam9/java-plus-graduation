package ru.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AggregationStarter {
    private final KafkaProducer producer;
    private final KafkaConsumer<Void, UserActionAvro> consumer;
    private final RecordHandler recordHandler;

    private static final Duration CONSUME_ATTEMPT_TIMEOUT = Duration.ofMillis(1000);
    @Value("${user.topic}")
    private String userTopic;
    @Value("${event.topic}")
    private String eventTopic;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public AggregationStarter(KafkaProducer producer,
                              KafkaConsumer<Void, UserActionAvro> consumer, RecordHandler recordHandler) {
        this.producer = producer;
        this.consumer = consumer;
        this.recordHandler = recordHandler;

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
    }

    //*
     //* Метод для начала процесса агрегации данных.
     //* Подписывается на топики для получения событий от датчиков,
     //* формирует снимок их состояния и записывает в кафку.

    public void start() {
        try {
            List<String> topics = List.of(userTopic);
            consumer.subscribe(topics);

            // Цикл обработки событий
            while (true) {
                ConsumerRecords<Void, UserActionAvro> records = consumer.poll(CONSUME_ATTEMPT_TIMEOUT);

                int count = 0;
                for (ConsumerRecord<Void, UserActionAvro> record : records) {
                    log.info("Received record {}", record.value());
                    List<EventSimilarityAvro> similaritys = recordHandler.updateState(record.value());
                    log.info("Будет отправлено {} similarity объектов", similaritys.size());
                    if (!similaritys.isEmpty()) {
                        for (EventSimilarityAvro similarity : similaritys) {
                            ProducerRecord<Void, EventSimilarityAvro> producerRecord = new ProducerRecord<>(eventTopic,
                                    similarity);

                            log.info("Отправка {} в топик {}", similarity, eventTopic);

                            try {
                                producer.send(producerRecord);
                                producer.flush();
                            } catch (Exception e) {
                                log.error("Ошибка при отправке сообщения в Kafka: {}", e.getMessage(), e);
                            }
                        }
                    }
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitSync();
            }

        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер в блоке finally
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от пользователей", e);
        } finally {

            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<Void, UserActionAvro> record, int count,
                                      KafkaConsumer<Void, UserActionAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
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
