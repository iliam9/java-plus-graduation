package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString
@Table(name = "weights")
@IdClass(SimilarityId.class)
public class EventSimilarity {

    @Id
    @Column(name = "id_event_a")
    private long eventA;

    @Id
    @Column(name = "id_event_b")
    private long eventB;

    private double score;

    @Column(name = "created")
    private Instant timestamp;
}