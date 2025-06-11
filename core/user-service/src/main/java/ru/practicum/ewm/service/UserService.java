package ru.practicum.ewm.service;

import ru.practicum.interaction.api.dto.user.NewUserRequest;
import ru.practicum.interaction.api.dto.user.UserDto;
import ru.practicum.interaction.api.dto.user.UserDtoForAdmin;

import java.util.List;

public interface UserService {

    List<UserDto> getAllUsers(List<Long> ids, Integer from, Integer size);

    UserDto saveUser(NewUserRequest newUserRequest);

    void deleteUser(Long id);

    UserDto findById(Long userId);

    UserDtoForAdmin adminFindById(Long userId);
}
