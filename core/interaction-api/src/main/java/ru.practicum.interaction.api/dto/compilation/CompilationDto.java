package ru.practicum.interaction.api.dto.compilation;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.interaction.api.dto.event.EventShortDto;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompilationDto {
    List<EventShortDto> events;
    Long id;
    Boolean pinned;
    String title;
}
