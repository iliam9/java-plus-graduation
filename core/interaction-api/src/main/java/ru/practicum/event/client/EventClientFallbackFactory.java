package ru.practicum.event.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.event.dto.*;
import ru.practicum.exception.DataAlreadyInUseException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ServiceTemporarilyUnavailable;

import java.util.List;

@Component
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

    @Override
    public EventClient create(Throwable cause) {
        return new EventClient() {
            @Override
            public List<EventFullDto> getEvents(EventDtoGetParam prm) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest rq) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public List<EventShortDto> getEventsForUser(EventDtoGetParam prm) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public EventFullDto getEventForUserById(Long userId, Long eventId, EventDtoGetParam prm) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest rq) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public List<EventShortDto> getPublicEvents(EventDtoGetParam prm) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public EventFullDto getPublicEventById(Long id) {
                if (cause instanceof FeignException e) {
                    if (e.status() == 404) {
                        throw new DataAlreadyInUseException(e.getMessage());
                    }
                    if (e.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        return new EventFullDto();
                    }
                }
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public EventFullDto getEvent(Long eventId) {
                if (cause instanceof FeignException e) {
                    if (e.status() == 404) {
                        throw new NotFoundException(e.getMessage());
                    }
                    if (e.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        return new EventFullDto();
                    }
                }
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }
        };
    }
}