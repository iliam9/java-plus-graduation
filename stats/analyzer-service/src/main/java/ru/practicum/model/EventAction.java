package ru.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Table(name = "event_actions")
public class EventAction {

    @EmbeddedId
    private EventActionId id;

    @Column(name = "scope_event")
    private Double score;

    @Column(name = "created")
    private Instant timestamp;
}
