package ru.practicum.compilation.dto;

import lombok.Data;
import ru.practicum.dto.events.EventDto;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    private List<EventDto> events;
    private boolean pinned = false;
    private String title;
}
