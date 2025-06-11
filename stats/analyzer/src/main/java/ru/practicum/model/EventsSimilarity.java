package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "events_similarities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventsSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a")
    private Long eventAId;

    @Column(name = "event_b")
    private Long eventBId;

    @Column(name = "score")
    private Double similarityScore;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

}
