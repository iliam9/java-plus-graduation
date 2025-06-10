package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.model.ActionState;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventUserRequest {


    Optional<@Size(min = 20, max = 2000) @NotBlank String> annotation;

    Optional<@Positive Long> category;

    Optional<@Size(min = 20, max = 7000) @NotBlank String> description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    Optional<@Future LocalDateTime> eventDate;

    Location location;

    Optional<@PositiveOrZero Long> participantLimit;

    Boolean requestModeration;

    ActionState stateAction;

    Boolean paid;

    Optional<@Size(min = 3, max = 120) @NotBlank String> title;
}
