package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaAvroProducer {

    private final Producer<Void, SpecificRecord> producer;

    public void send(String topic, SpecificRecordBase data) {
        ProducerRecord<Void, SpecificRecord> record = new ProducerRecord<>(topic, data);

        log.info("Отправка {} в топик {}", data, topic);

        try {
            producer.send(record);
            producer.flush();
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения в Kafka: {}", e.getMessage(), e);
        }
    }
}
