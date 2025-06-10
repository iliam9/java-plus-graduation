package ru.practicum.ewm.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class EventWithInitiatorDto {
    long id;
    long initiatorId;
    EventState state;
    long participantLimit;
    boolean requestModeration;
}
