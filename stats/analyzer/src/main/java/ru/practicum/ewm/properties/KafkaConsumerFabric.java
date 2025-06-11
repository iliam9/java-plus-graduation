package ru.practicum.ewm.properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConsumerFabric {

    @Value("${spring.kafka.consumer.user-action-client-id}")
    private String userActionClientId;
    @Value("${spring.kafka.consumer.user-action-group-id}")
    private String userActionGroupId;

    @Value("${spring.kafka.consumer.similarity-client-id}")
    private String similarityClientId;
    @Value("${spring.kafka.consumer.similarity-group-id}")
    private String similarityGroupId;

    @Value("${spring.kafka.bootstrap-server}")
    private String bootstrapServer;
    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;
    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;
    @Value("${spring.kafka.consumer.similarity-deserializer}")
    private String similarityDeserializer;
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private boolean enableAutoCommit;

    @Bean
    public Consumer<Long, UserActionAvro> userActionConsumerProperties() {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, userActionClientId);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, userActionGroupId);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);

        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                keyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                valueDeserializer);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                enableAutoCommit);

        return new KafkaConsumer<>(properties);
    }

    @Bean
    public Consumer<Long, EventSimilarityAvro> eventSimilarityConsumerProperties() {

        Properties properties = new Properties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, similarityClientId);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, similarityGroupId);
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                keyDeserializer);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                similarityDeserializer);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                enableAutoCommit);

        return new KafkaConsumer<>(properties);
    }
}
