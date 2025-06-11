package ru.practicum.ewm.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.interaction.api.dto.user.UserDto;
import ru.practicum.interaction.api.dto.user.UserShortDto;
import ru.practicum.interaction.api.enums.event.State;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.interaction.api.dto.event.EventFullDto;
import ru.practicum.interaction.api.dto.event.EventShortDto;
import ru.practicum.interaction.api.dto.event.Location;
import ru.practicum.interaction.api.dto.event.NewEventDto;

import java.time.LocalDateTime;

@UtilityClass
public class EventMapper {

    public Event mapToEvent(NewEventDto eventDto, Category category, Long initiatorId) {
        return Event.builder()
                .eventDate(eventDto.getEventDate())
                .annotation(eventDto.getAnnotation())
                .paid(eventDto.getPaid())
                .category(category)
                .confirmedRequests(0)
                .createdOn(LocalDateTime.now())
                .description(eventDto.getDescription())
                .state(State.PENDING)
                .title(eventDto.getTitle())
                .lat(eventDto.getLocation().getLat())
                .lon(eventDto.getLocation().getLon())
                .participantLimit(eventDto.getParticipantLimit())
                .requestModeration(eventDto.getRequestModeration())
                .initiatorId(initiatorId)
                .commenting(eventDto.getCommenting())
                .build();
    }

    public EventFullDto mapToFullDto(Event event, Double rating, UserDto userDto) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserShortDto.builder()
                        .name(userDto.getName())
                        .id(userDto.getId())
                        .build().getId())
                .location(Location.builder().lat(event.getLat()).lon(event.getLon()).build())
                .paid(event.getPaid())
                .rating(rating)
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .commenting(event.getCommenting())
                .build();
    }

    public EventShortDto mapToShortDto(Event event, Double rating, UserDto userDto) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate())
                .publishedOn(event.getPublishedOn())
                .id(event.getId())
                .initiator(UserShortDto.builder()
                        .id(userDto.getId())
                        .name(userDto.getName())
                        .build().getId())
                .paid(event.getPaid())
                .title(event.getTitle())
                .rating(rating)
                .commenting(event.getCommenting())
                .build();
    }
}
