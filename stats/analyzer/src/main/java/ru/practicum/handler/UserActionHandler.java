package ru.practicum.handler;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.configuration.ScoreSettings;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.ActionType;
import ru.practicum.model.UserAction;
import ru.practicum.repository.UserActionRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserActionHandler {

    private final ScoreSettings scoreSettings;

    private final UserActionRepository userActionRepository;

    public void handleUserAction(UserActionAvro userActionAvro) {
        UserAction userAction = UserAction.builder()
                .userId( userActionAvro.getUserId())
                .eventId( userActionAvro.getEventId())
                .actionType(ActionType.valueOf(userActionAvro.getActionType().name()))
                .timestamp(LocalDateTime.ofInstant(userActionAvro.getTimestamp(), ZoneId.of("UTC")))
                .build();
        Optional<UserAction> existedUserAction = userActionRepository.findByUserIdAndEventId(userAction.getUserId(), userAction.getEventId());
        if (existedUserAction.isPresent()) {
            UserAction existed = existedUserAction.get();
            if (getScoreByActionType(existed.getActionType()) < getScoreByActionType(userAction.getActionType())) {
                existed.setActionType(userAction.getActionType());
                existed.setTimestamp(userAction.getTimestamp());
                userActionRepository.save(existed);
            }
        } else {
            userActionRepository.save(userAction);
        }
    }

    public double getScoreByActionType(ActionType actionType) {
        double score = 0;
        switch (actionType) {
            case VIEW:
                score = scoreSettings.getView();
                break;
            case REGISTER:
                score = scoreSettings.getRegistration();
                break;
            case LIKE:
                score = scoreSettings.getLike();
                break;
            default:
                throw new ValidationException("Передан некорректный тип действия пользователя.");
        }
        return score;
    }

}
