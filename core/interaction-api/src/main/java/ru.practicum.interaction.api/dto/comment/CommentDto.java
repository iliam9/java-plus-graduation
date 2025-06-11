package ru.practicum.interaction.api.dto.comment;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import static ru.practicum.interaction.api.utils.date.DateTimeFormat.TIME_PATTERN;

@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CommentDto {
    Long id;
    String text;
    Long event;
    String eventName;
    String authorName;
    Integer likes;
    @JsonFormat(pattern = TIME_PATTERN)
    LocalDateTime created;
}
