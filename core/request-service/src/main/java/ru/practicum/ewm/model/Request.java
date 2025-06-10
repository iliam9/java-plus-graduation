package ru.practicum.ewm.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "requests")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Request {

    public Request() {
    }

    public Request(Long eventId, Long userId) {
        this.eventId = eventId;
        this.userId = userId;
        status = RequestStatus.PENDING;
        created = LocalDateTime.now();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "event_id")
    long eventId;

    @Column(name = "user_id")
    Long userId;

    @Enumerated(value = EnumType.ORDINAL)
    RequestStatus status;

    LocalDateTime created;
}
