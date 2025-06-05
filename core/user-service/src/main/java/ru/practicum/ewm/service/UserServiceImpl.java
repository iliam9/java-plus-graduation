package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.UserShortDto;
import ru.practicum.ewm.exception.ConflictDataException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.UserMapper;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.dto.FindUsersParams;
import ru.practicum.ewm.dto.NewUserRequest;
import ru.practicum.ewm.dto.UserDto;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto createUser(NewUserRequest request) {
        User user = userMapper.mapToUser(request);
        isEmailUnique(user.getEmail());
        user = userRepository.save(user);
        log.info("User is saved: {}", user);
        return userMapper.mapToUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findUsers(FindUsersParams params) {
        List<User> users;
        if (params.getIds() == null || params.getIds().isEmpty()) {
            users = userRepository.findAllOrderById(params.getFrom(), params.getSize());
        } else {
            users = userRepository.findAllByIds(params.getIds());
        }
        log.info("Search for users completed");
        return userMapper.mapToUsersDto(users);
    }

    @Override
    public void deleteUser(long id) {
        User userToDelete = userRepository.findById(id).orElseThrow(() -> {
                    log.error("Not found user with ID = {}", id);
                    return new NotFoundException("Not found user with ID = " + id);
                });
        userRepository.delete(userToDelete);
    }

    @Override
    public List<UserShortDto> findShortUsers(List<Long> ids) {
        List<User> users = userRepository.findAllByIds(ids);
        log.info("Search for users with ids {} completed", ids);
        return userMapper.mapToUserShortDto(users);
    }

    private void isEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Email {} is already registered", email);
            throw new ConflictDataException("This email is already registered");
        }
    }
}
