package ru.practicum.compilation.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "compilations")
public class Compilation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection(targetClass=Long.class)
    @CollectionTable(
            name="compilations_events",
            joinColumns=@JoinColumn(name="compilation_id"))
    @Column(name="event_id")
    private List<Long> events = new ArrayList<>();
    private boolean pinned;
    private String title;
}
