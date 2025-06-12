package ru.practicum.mapping;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.model.UserAction;
import ru.practicum.model.UserActionId;
import ru.practicum.model.UserActionType;

public class UserMapper {

    public static UserAction map(UserActionAvro userActionAvro) {
        UserAction userAction = new UserAction();
        UserActionId userActionId = new UserActionId();
        userActionId.setUserId(userActionAvro.getUserId());
        userActionId.setEventId(userActionAvro.getEventId());
        userAction.setId(userActionId);
        userAction.setActionType(UserActionType.valueOf(userActionAvro.getActionType().name()));
        userAction.setTimestamp(userActionAvro.getTimestamp());
        return userAction;
    }
}
