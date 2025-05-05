package ru.practicum.ewm.user.service;

import ru.practicum.ewm.user.dto.FindUsersParams;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> findUsers(FindUsersParams params);

    void deleteUser(long id);
}
