package ru.practicum.configuration;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {

    @Value("${collector.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${collector.kafka.key-serializer}")
    private String keySerializer;

    @Value("${collector.kafka.value-serializer}")
    private String valueSerializer;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> userActionProducer() {
        Properties kafkaConfigs = new Properties();
        kafkaConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        kafkaConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return new KafkaProducer<>(kafkaConfigs);
    }

}
