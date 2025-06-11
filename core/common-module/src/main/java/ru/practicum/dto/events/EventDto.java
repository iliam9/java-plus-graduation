package ru.practicum.dto.events;

import lombok.Data;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.users.UserShortDto;

import java.util.List;

@Data
public class EventDto {
    private Long id;
    private String title;
    private String annotation;
    private String description;

    private long confirmedRequests;
    private double rating;

    private boolean paid;
    private boolean requestModeration;
    private int participantLimit;

    private CategoryDto category;
    private LocationDto location;

    private EventState state;
    private UserShortDto initiator;

    private String eventDate;
    private String createdOn;
    private String publishedOn;

    private List<CommentDto> comments;
}
