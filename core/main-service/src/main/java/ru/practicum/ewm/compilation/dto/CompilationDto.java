package ru.practicum.ewm.compilation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.event.dto.EventShortDto;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    @NotNull
    Long id;
    Boolean pinned;
    @NotNull
    String title;
    List<EventShortDto> events;
}
