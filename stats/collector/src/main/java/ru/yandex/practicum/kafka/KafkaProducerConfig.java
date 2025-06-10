package ru.yandex.practicum.kafka;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.apache.kafka.common.utils.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import ru.practicum.kafka.serializer.GeneralKafkaSerializer;

import java.util.Properties;

@Configuration
public class KafkaProducerConfig {
    @Value(value = "${kafkaServer}")
    private String bootstrapAddress;

    @Bean
    @Scope("prototype")
    KafkaClient getClient() {
        return new KafkaClient() {

            private Producer<String, SpecificRecordBase> producer;

            @Override
            public Producer<String, SpecificRecordBase> getProducer() {
                if (producer == null) {
                    initProducer();
                }
                return producer;
            }

            private void initProducer() {
                Properties config = new Properties();
                config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
                config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, VoidSerializer.class);
                config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralKafkaSerializer.class);

                producer = new KafkaProducer<>(config);
            }

            @Override
            public void stop() {
                if (producer != null) {
                    producer.flush();
                    Utils.sleep(100000);
                    producer.close();
                }
            }
        };
    }
}
