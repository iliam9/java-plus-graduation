package ru.practicum.event.mapper;

import org.mapstruct.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;

import java.util.List;


@Mapper(componentModel = "spring", uses = {LocationMapper.class})
public interface EventMapper {
    //target - поле на выходе, source на входе
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "rating", ignore = true)
    EventFullDto toEventFullDto(Event event);

    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "createdOn", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "state", expression = "java(ru.practicum.event.dto.State.PENDING)")
    @Mapping(target = "participantLimit", source = "participantLimit", defaultValue = "0")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    Event toEntity(NewEventDto newEventDto);

    List<EventShortDto> toEventShortDto(List<Event> events);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)//игнорирование полей c null
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    void updateFromAdmin(UpdateEventAdminRequest rq, @MappingTarget Event event); //@MappingTarget указывает что целевой объект, в который будут копироваться значения, уже существует и будет обновлен

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category.id", source = "category")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "state", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    void updateFromUser(UpdateEventUserRequest rq, @MappingTarget Event event);
}
