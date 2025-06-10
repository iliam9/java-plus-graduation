package ru.yandex.practicum;

import lombok.Getter;
import lombok.Setter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.Properties;
import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties("aggregator")
public class AggregatorConfig {

    private String similarityTopic;
    private List<String> sensorTopic;
    private Duration consumeAttemptTimeout;
    private Properties producerProperties;
    private Properties consumerProperties;

    @Bean
    public KafkaProducer<String, SpecificRecordBase> producer() throws Exception {
        return new KafkaProducer<>(getProducerProperties());
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> consumer() throws Exception {
        return new KafkaConsumer<>(getConsumerProperties());
    }
}