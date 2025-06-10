package ru.practicum.ewm.dto.compilation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class GetCompilationsParams {
    int size;
    int from;
    Boolean pinned;
}
