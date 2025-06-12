package ru.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Table(name = "user_actions")
public class UserAction {

    @EmbeddedId
    private UserActionId id;

    @Enumerated(EnumType.STRING)
    private UserActionType actionType;

    @Column(name = "created")
    private Instant timestamp;
}
