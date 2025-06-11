package ru.practicum.interaction.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.*;
import ru.practicum.interaction.api.enums.request.Status;

import java.time.LocalDateTime;

import static ru.practicum.interaction.api.utils.date.DateTimeFormat.TIME_PATTERN;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    Long id;
    @JsonFormat(pattern = TIME_PATTERN)
    LocalDateTime created;
    Long event;
    Long requester;
    Status status;
}
