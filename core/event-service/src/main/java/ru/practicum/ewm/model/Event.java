package ru.practicum.ewm.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.dto.EventState;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "events")
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String annotation;

    @ManyToOne
    @JoinColumn(name = "category_id")
    Category category;

    @Column(name = "user_id")
    Long initiatorId;

    @Column(name = "user_name")
    String initiatorName;

    @Column(name = "created")
    LocalDateTime createdOn;

    String description;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    double latitude;

    double longitude;

    @Column(name = "participant_limit")
    long participantLimit;

    @Column(name = "published")
    LocalDateTime publishedOn;

    @Column(name = "request_moderation")
    boolean requestModeration;

    @Column(name = "state")
    @Enumerated(value = EnumType.ORDINAL)
    EventState state;

    boolean paid;

    String title;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Event event = (Event) object;
        return id == event.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

