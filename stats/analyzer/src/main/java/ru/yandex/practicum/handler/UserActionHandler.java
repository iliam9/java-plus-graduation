package ru.yandex.practicum.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mapper.AnalyzerMapper;
import ru.yandex.practicum.model.UserAction;
import ru.yandex.practicum.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class UserActionHandler {
    private final AnalyzerMapper mapper;
    private final UserActionRepository repository;

    public void handle(UserActionAvro userActionAvro) {
        UserAction userAction = mapper.map(userActionAvro);
        repository.save(userAction);
        log.info("Saved user ID = {} weight: {}", userAction.getUserId(),userAction.getWeight());
    }
}
