package ru.practicum.ewm.event.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class, CategoryMapper.class}, imports = {LocalDateTime.class})
public interface EventMapper {

    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(expression = "java(newEvent.getLocation().getLat())", target = "latitude")
    @Mapping(expression = "java(newEvent.getLocation().getLon())", target = "longitude")
    @Mapping(target = "publishedOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "category", ignore = true)
    Event map(NewEventDto newEvent);

    @Mapping(source = "views", target = "views")
    @Mapping(expression = "java(map(event))", target = "location")
    @Mapping(source = "countConfirmedRequest", target = "confirmedRequests")
    EventFullDto mapToFullDto(Event event, Long views, Long countConfirmedRequest);

    @Mapping(source = "hits", target = "views")
    @Mapping(source = "count", target = "confirmedRequests")
    EventShortDto mapToShortDto(Event event, Long hits, Long count);

    @Mapping(source = "latitude", target = "lat")
    @Mapping(source = "longitude", target = "lon")
    Location map(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "category", target = "category")
    @Mapping(source = "updateEvent.annotation", target = "annotation", qualifiedByName = "unwrap")
    @Mapping(source = "updateEvent.description", target = "description", qualifiedByName = "unwrap")
    @Mapping(source = "updateEvent.title", target = "title", qualifiedByName = "unwrap")
    @Mapping(source = "updateEvent.participantLimit", target = "participantLimit", qualifiedByName = "unwrap")
    @Mapping(source = "updateEvent.eventDate", target = "eventDate", qualifiedByName = "unwrap")
    void update(@MappingTarget Event event, UpdateEventUserRequest updateEvent, Category category);

    @Named(value = "unwrap")
    default <T> T unwrap(Optional<T> optional) {
        return optional.orElse(null);
    }
}