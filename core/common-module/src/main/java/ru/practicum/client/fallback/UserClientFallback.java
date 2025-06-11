package ru.practicum.client.fallback;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.client.UserClient;
import ru.practicum.dto.users.UserDto;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class UserClientFallback implements UserClient {

    private static final String SERVICE_UNAVAILABLE = "Сервис 'Администрирование пользователей' временно недоступен: ";


    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.warn(SERVICE_UNAVAILABLE + "невозможно получить пользователей по id.");
        return Collections.emptyList();
    }

}
