package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.client.fallback.UserClientFallback;
import ru.practicum.dto.users.UserDto;

import java.util.List;

@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    /**
     * Получение пользователей по их идентификаторам.
     *
     * @param ids  список идентификаторов запрашиваемых пользователей
     * @param from смещение относительно начала списка пользователей
     * @param size количество пользователей, выводимых на одной странице
     * @return список пользователей.
     */
    @GetMapping("/admin/users")
    List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                           @RequestParam(defaultValue = "0") int from,
                           @RequestParam(defaultValue = "10") int size);

}
