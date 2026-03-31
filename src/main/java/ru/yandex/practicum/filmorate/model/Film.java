package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Duration;
import java.time.LocalDate;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {

    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private long duration;
}