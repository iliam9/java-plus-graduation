package ru.practicum.ewm.stats.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ParamStat {
    LocalDateTime start;
    LocalDateTime end;
    List<String> uris;
    Boolean unique;
}
