package ru.practicum.ewm.stats.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class Stat {
    String app;
    String uri;
    long hits;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stat stat)) return false;
        return (Objects.equals(app, stat.getApp()) &&
                Objects.equals(uri, stat.getUri()) &&
                Objects.equals(hits, stat.getHits()));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
