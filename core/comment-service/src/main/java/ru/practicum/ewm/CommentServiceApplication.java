package ru.practicum.ewm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.practicum.interaction.api.feignClient.client.event.AdminEventClient;
import ru.practicum.interaction.api.feignClient.client.user.UserClient;

@SpringBootApplication
@EnableFeignClients(clients = {UserClient.class, AdminEventClient.class, })
public class CommentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}
