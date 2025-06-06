package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.dto.event.EventShortDto;

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
