package ru.practicum.ewm.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.errorHandler.exception.ConflictDataException;
import ru.practicum.ewm.errorHandler.exception.NotFoundException;
import ru.practicum.ewm.user.dto.FindUsersParams;
import ru.practicum.ewm.user.dto.NewUserRequest;
import ru.practicum.ewm.user.dto.UserDto;
import ru.practicum.ewm.user.mapper.UserMapper;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

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

    private void isEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            log.error("Email {} is already registered", email);
            throw new ConflictDataException("This email is already registered");
        }
    }
}
