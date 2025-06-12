package ru.practicum.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserService {

    void update(UserActionAvro userActionAvro);
}
