package ru.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class EventActionId {

    @Column(name = "event_a")
    private Long eventA;

    @Column(name = "event_b")
    private Long eventB;
}
