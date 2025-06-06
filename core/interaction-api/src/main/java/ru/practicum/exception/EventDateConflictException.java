package ru.practicum.exception;

public class EventDateConflictException extends RuntimeException {
    public EventDateConflictException(String message) {
        super(message);
    }
}
