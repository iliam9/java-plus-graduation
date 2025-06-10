package ru.yandex.practicum;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Properties;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties("analyzer")
public class AnalyzerConfig {
    private List<String> actionTopics;
    private Duration actionConsumeAttemptTimeout;
    private Properties actionConsumerProperties;
    private List<String> similarityTopics;
    private Duration similarityConsumeAttemptTimeout;
    private Properties similarityConsumerProperties;

    @Bean
    public KafkaConsumer<String, UserActionAvro> userConsumer() throws Exception {
        return new KafkaConsumer<>(getActionConsumerProperties());
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> eventConsumer() throws Exception {
        return new KafkaConsumer<>(getSimilarityConsumerProperties());
    }
}