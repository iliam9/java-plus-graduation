package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@Table(name = "user_actions")
public class UserAction implements Comparable<UserAction> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;

    private long eventId;

    private double weight;

    @Column(name = "created")
    private Instant timestamp;

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        UserAction that = (UserAction) object;
        return userId == that.userId && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, timestamp);
    }

    @Override
    public int compareTo(UserAction o) {
        return timestamp.compareTo(o.getTimestamp());
    }
}
