package ru.practicum.ewm.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.model.UserAction;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@UtilityClass
public class UserActionMapper {

    public UserAction mapToUserAction(UserActionAvro userActionAvro) {
        return UserAction.builder()
                .eventId(userActionAvro.getEventId())
                .userId(userActionAvro.getUserId())
                .mark(mapToType(userActionAvro.getActionType()))
                .timestamp(userActionAvro.getTimestamp())
                .build();
    }

    private Float mapToType(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4f;
            case REGISTER -> 0.8f;
            case LIKE -> 1.0f;
        };
    }
}
