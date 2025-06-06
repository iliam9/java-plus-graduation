package ru.practicum.ewm.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationMapper {

    CompilationDto mapToCompilationDto(Compilation compilation);

    @Mapping(target = "events", source = "events")
    Compilation mapToCompilation(NewCompilationDto compilationDto, List<Event> events);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "events", target = "events")
    @Mapping(source = "request.title", target = "title", qualifiedByName = "unwrap")
    Compilation update(@MappingTarget Compilation compilation, UpdateCompilationRequest request, Set<Event> events);

    @Named(value = "unwrap")
    default <T> T unwrap(Optional<T> optional) {
        return optional.orElse(null);
    }


}
