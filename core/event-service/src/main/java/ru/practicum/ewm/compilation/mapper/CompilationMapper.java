package ru.practicum.ewm.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.compilation.model.Compilation;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.interaction.api.dto.compilation.NewCompilationDto;
import ru.practicum.interaction.api.dto.compilation.CompilationDto;
import ru.practicum.interaction.api.dto.event.EventShortDto;

import java.util.HashSet;
import java.util.List;

@UtilityClass
public class CompilationMapper {

    public Compilation toCompilation(NewCompilationDto compilationDto, List<Event> events) {
        return Compilation.builder()
                .pinned(compilationDto.getPinned())
                .title(compilationDto.getTitle())
                .events(new HashSet<>(events))
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation, List<EventShortDto> listEventDto) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(listEventDto)
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();


    }
}
