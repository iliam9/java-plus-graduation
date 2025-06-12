package ru.practicum.request.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.dto.EventFullDto;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestStatusUpdateDto {
    private EventRequestStatusUpdateRequest updateRequest;
    private EventFullDto event;
}
