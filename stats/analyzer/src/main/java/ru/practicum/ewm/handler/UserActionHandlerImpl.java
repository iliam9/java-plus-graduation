package ru.practicum.ewm.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.mapper.UserActionMapper;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.repository.UserActionRepository;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserActionHandlerImpl implements UserActionHandler {

    private final UserActionRepository userActionRepository;

    @Value("${application.action-weight.view}")
    private float view;
    @Value("${application.action-weight.register}")
    private float register;
    @Value("${application.action-weight.like}")
    private float like;

    @Transactional
    @Override
    public void handle(UserActionAvro action) {
        Long eventId = action.getEventId();
        Long userId = action.getUserId();
        Float newActionMark = switch (action.getActionType()) {
            case LIKE -> like;
            case REGISTER -> register;
            case VIEW -> view;
        };

        if (!userActionRepository.existsByEventIdAndUserId(eventId, userId)) {
            userActionRepository.save(UserActionMapper.mapToUserAction(action));
        } else {
            UserAction userAction = userActionRepository.findByEventIdAndUserId(eventId, userId);
            if (userAction.getMark() < newActionMark) {
                userAction.setMark(newActionMark);
                userAction.setTimestamp(action.getTimestamp());
            }
        }
    }
}
