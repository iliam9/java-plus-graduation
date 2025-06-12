package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.processor.EventProcessor;
import ru.practicum.processor.UserProcessor;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);

        final EventProcessor eventProcessor = context.getBean(EventProcessor.class);
        UserProcessor userProcessor = context.getBean(UserProcessor.class);

        Thread eventThread = new Thread(eventProcessor);
        eventThread.setName("eventHandlerThread");
        eventThread.start();

        userProcessor.start();
    }
}