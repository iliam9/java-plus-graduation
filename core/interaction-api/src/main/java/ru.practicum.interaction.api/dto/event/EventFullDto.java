package ru.practicum.interaction.api.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.interaction.api.dto.category.CategoryDto;
import ru.practicum.interaction.api.enums.event.State;

import java.time.LocalDateTime;

import static ru.practicum.interaction.api.utils.date.DateTimeFormat.TIME_PATTERN;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EventFullDto {
    Long id;
    String annotation;
    CategoryDto category;
    Integer confirmedRequests;
    @JsonFormat(pattern = TIME_PATTERN)
    LocalDateTime createdOn;
    LocalDateTime publishedOn;
    String description;
    @JsonFormat(pattern = TIME_PATTERN)
    LocalDateTime eventDate;
    Long initiator;
    Location location;
    Boolean paid;
    Integer participantLimit;
    State state;
    Boolean requestModeration;
    String title;
    Double rating;
    Boolean commenting;
}
