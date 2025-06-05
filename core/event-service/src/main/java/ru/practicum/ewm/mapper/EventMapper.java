package ru.practicum.ewm.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.Location;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.dto.EventWithInitiatorDto;
import ru.practicum.ewm.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.Optional;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CategoryMapper.class}, imports = {LocalDateTime.class})
public interface EventMapper {

    @Mapping(target = "eventDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    @Mapping(expression = "java(newEvent.getLocation().getLat())", target = "latitude")
    @Mapping(expression = "java(newEvent.getLocation().getLon())", target = "longitude")
    @Mapping(target = "publishedOn", expression = "java(LocalDateTime.now())")
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "initiatorName", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    Event map(NewEventDto newEvent);

    @Mapping(source = "views", target = "views")
    @Mapping(expression = "java(getLocation(event))", target = "location")
    @Mapping(expression = "java(getUser(event))", target = "initiator")
    @Mapping(source = "countConfirmedRequest", target = "confirmedRequests")
    EventFullDto mapToFullDto(Event event, Long views, Long countConfirmedRequest);

    @Mapping(source = "hits", target = "views")
    @Mapping(source = "countConfirmedRequest", target = "confirmedRequests")
    @Mapping(expression = "java(getUser(event))", target = "initiator")
    EventShortDto mapToShortDto(Event event, Long hits, Long countConfirmedRequest);

    EventWithInitiatorDto mapToInitiatorDto(Event event);

    @Mapping(source = "latitude", target = "lat")
    @Mapping(source = "longitude", target = "lon")
    Location getLocation(Event event);

    @Mapping(source = "initiatorId", target = "id")
    @Mapping(source = "initiatorName", target = "name")
    UserShortDto getUser(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiatorId", ignore = true)
    @Mapping(target = "initiatorName", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
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