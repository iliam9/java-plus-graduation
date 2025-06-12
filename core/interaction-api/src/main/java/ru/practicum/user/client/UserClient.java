package ru.practicum.user.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.dto.UsersDtoGetParam;

import java.util.List;

@FeignClient(name = "user-service", path = "/admin/users", fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient {

    @GetMapping
    ResponseEntity<List<UserDto>> getUsers(@ModelAttribute @Valid UsersDtoGetParam usersDtoGetParam);

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<UserDto> createUser(@RequestBody @Valid UserDto userDto);

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    ResponseEntity<String> deleteUser(@PathVariable Long userId);

    @GetMapping("/{userId}")
    ResponseEntity<UserShortDto> getUser(@PathVariable Long userId);
}
