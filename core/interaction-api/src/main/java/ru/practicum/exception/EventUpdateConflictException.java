package ru.practicum.exception;

public class EventUpdateConflictException extends RuntimeException {
    public EventUpdateConflictException(String message) {
        super(message);
    }
}
