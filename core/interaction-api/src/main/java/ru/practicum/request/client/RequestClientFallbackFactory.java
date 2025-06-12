package ru.practicum.request.client;

import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.practicum.exception.DataAlreadyInUseException;
import ru.practicum.exception.ServiceTemporarilyUnavailable;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.dto.RequestStatusUpdateDto;

import java.util.List;

@Component
public class RequestClientFallbackFactory implements FallbackFactory<RequestClient> {

    @Override
    public RequestClient create(Throwable cause) {
        return new RequestClient() {

            @Override
            public ParticipationRequestDto create(long userId, long eventId) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public List<ParticipationRequestDto> getAllByParticipantId(long userId) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public ParticipationRequestDto cancelParticipantRequest(long userId, long requestId) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public List<ParticipationRequestDto> getAllByEventId(long userId, long eventId) {
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }

            @Override
            public EventRequestStatusUpdateResult updateRequestStatus(long userId, RequestStatusUpdateDto requestStatusUpdateDto) {
                if (cause instanceof FeignException e) {
                    if (e.status() == 409) {
                        throw new DataAlreadyInUseException(e.getMessage());
                    }
                    if (e.status() == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                        return new EventRequestStatusUpdateResult();
                    }
                }
                throw new ServiceTemporarilyUnavailable(cause.getMessage());
            }
        };
    }
}
