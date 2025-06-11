package ru.practicum.event.dto;

import lombok.Data;
import ru.practicum.dto.categories.CategoryDto;
import ru.practicum.dto.users.UserShortDto;

@Data
public class EventShortDto {
    private Long id;
    private String title;
    private String annotation;

    private Long confirmedRequests;
    private Double rating;

    private boolean paid;

    private CategoryDto category;

    private UserShortDto initiator;

    private String eventDate;
}