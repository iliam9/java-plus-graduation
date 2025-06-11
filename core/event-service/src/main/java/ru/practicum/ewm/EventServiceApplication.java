package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.interaction.api.feignClient.client.request.AdminParticipationRequestClient;
import ru.practicum.interaction.api.feignClient.client.stat.StatClient;
import ru.practicum.interaction.api.feignClient.client.user.UserClient;

@SpringBootApplication
@EnableFeignClients(clients = {StatClient.class, UserClient.class, AdminParticipationRequestClient.class})
public class EventServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventServiceApplication.class, args);
    }

}
