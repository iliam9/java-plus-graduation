package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.users.UserDto;
import ru.practicum.service.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/admin/users")
public class AdminUserController {

    private final UserService userService;


    /**
     * Получить список пользователей по их идентификаторам.
     *
     * @param ids  список идентификаторов запрашиваемых пользователей
     * @param from смещение от начала возвращаемого списка пользователей
     * @param size размер возвращаемого списка пользователей
     * @return список пользователей
     */
    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        return userService.getUsers(ids, from, size);
    }

    /**
     * Добавить нового пользователя в систему.
     *
     * @param userDto представление добавляемого пользователя
     * @return представление добавленного пользователя
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    /**
     * Удалить пользователя.
     *
     * @param id идентификатор удаляемого пользователя
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}
