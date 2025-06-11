package ru.practicum.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.repository.EventRepository;

import java.util.Optional;


@RequiredArgsConstructor
@Component
public final class CompilationMapperImpl {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public CompilationDto toDto(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setPinned(compilation.isPinned());
        dto.setTitle(compilation.getTitle());
        dto.setEvents(compilation.getEvents().stream()
                .map(eventRepository::findById)
                .map(Optional::get)
                .map(eventMapper::toDto)
                .toList());
        return dto;
    }

}
