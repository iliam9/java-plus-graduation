package ru.practicum.config;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.serializer.EventAvroSerializer;

import java.util.Properties;

@Slf4j
@Configuration
public class CollectorConfig {
    @Value("${kafka.url}")
    private String bootstrapServers;

    private Producer<Void, SpecificRecord> producer;

    @Bean
    public Producer<Void, SpecificRecord> producer() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EventAvroSerializer.class);
        producer = new KafkaProducer<>(config);
        log.info("Kafka-продюсер создан.");
        return producer;
    }

    /* Метод для закрытия продюсера, закрытие происходит только в конце работы приложения,
    чтобы экономить ресурсы и не создавать каждый раз продюсера на каждую отправку сообщения в kafka. */
    @PreDestroy
    public void closeProducer() {
        if (producer != null) {
            producer.close();
            log.info("Kafka-продюсер закрыт.");
        }
    }
}