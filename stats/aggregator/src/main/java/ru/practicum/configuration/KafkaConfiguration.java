package ru.practicum.configuration;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KafkaConfiguration {

    @Value("${aggregator.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${aggregator.kafka.consumer.client-id}")
    private String consumerClientId;

    @Value("${aggregator.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${aggregator.kafka.consumer.key-deserializer}")
    private String consumerKeyDeserializer;

    @Value("${aggregator.kafka.consumer.value-deserializer}")
    private String consumerValueDeserializer;

    @Value("${aggregator.kafka.producer.key-serializer}")
    private String producerKeySerializer;

    @Value("${aggregator.kafka.producer.value-serializer}")
    private String producerValueSerializer;

    @Bean
    public KafkaConsumer<String, SpecificRecordBase> consumer() {
        Properties kafkaConfigs = new Properties();
        kafkaConfigs.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerClientId);
        kafkaConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        kafkaConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerKeyDeserializer);
        kafkaConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerValueDeserializer);
        return new KafkaConsumer<>(kafkaConfigs);
    }

    @Bean
    public KafkaProducer<String, SpecificRecordBase> producer() {
        Properties kafkaConfigs = new Properties();
        kafkaConfigs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        kafkaConfigs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerKeySerializer);
        kafkaConfigs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerValueSerializer);
        kafkaConfigs.put(ProducerConfig.LINGER_MS_CONFIG, 3000);
        return new KafkaProducer<>(kafkaConfigs);
    }

}
