package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.mapping.UserMapper;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void update(UserActionAvro userActionAvro) {
        log.info("Save UserAction: {}", userActionAvro);
        UserAction userAction = userRepository.save(UserMapper.map(userActionAvro));
        log.info("Saved UserAction: {}", userAction);
    }
}
