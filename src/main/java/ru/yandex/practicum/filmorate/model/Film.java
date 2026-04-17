package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

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
    public Set<Integer> likes;
}