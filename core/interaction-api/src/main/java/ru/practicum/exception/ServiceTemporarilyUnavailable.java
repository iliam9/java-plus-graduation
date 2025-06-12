package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceTemporarilyUnavailable extends RuntimeException {
    public ServiceTemporarilyUnavailable(String message) {
        super(message);
        log.error(message);
    }
}
