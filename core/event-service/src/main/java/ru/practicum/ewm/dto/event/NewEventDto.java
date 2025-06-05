package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {


    @Size(min = 20, max = 2000)
    @NotNull
    @NotBlank
    String annotation;

    @Positive
    @NotNull
    long category;


    @Size(min = 20, max = 7000)
    @NotNull
    @NotBlank
    String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Future
    @NotNull
    LocalDateTime eventDate;

    @NotNull
    Location location;


    @NotNull
    boolean paid;

    @PositiveOrZero
    @NotNull
    long participantLimit;

    @NotNull
    boolean requestModeration = true;

    @Size(min = 3, max = 120)
    @NotBlank
    @NotNull
    String title;
}
