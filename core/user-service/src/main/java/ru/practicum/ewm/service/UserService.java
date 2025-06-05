package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.dto.FindUsersParams;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserRequest request);

    List<UserDto> findUsers(FindUsersParams params);

    void deleteUser(long id);

    List<UserShortDto> findShortUsers(List<Long> ids);
}
