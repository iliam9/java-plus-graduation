package ru.yandex.practicum.handler;

import ru.practicum.ewm.stats.proto.UserActionProto;

public interface UserActionHandler {

    void handle(UserActionProto event);
}
