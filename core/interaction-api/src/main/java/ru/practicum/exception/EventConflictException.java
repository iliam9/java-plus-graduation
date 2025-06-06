package ru.practicum.exception;

public class EventConflictException extends RuntimeException {
    private final String reason;
    private final String error;

    public EventConflictException(String message, String reason) {
        super(message);
        this.reason = reason;
        this.error = "For the requested operation the conditions are not met.";
    }

    public String getReason() {
        return reason;
    }

    public String getError() {
        return error;
    }
}