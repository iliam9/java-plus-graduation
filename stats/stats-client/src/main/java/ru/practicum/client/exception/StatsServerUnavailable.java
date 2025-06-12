package ru.practicum.client.exception;

public class StatsServerUnavailable extends RuntimeException {
    public StatsServerUnavailable(String s, Exception e) {
        super(s, e);
    }
}
