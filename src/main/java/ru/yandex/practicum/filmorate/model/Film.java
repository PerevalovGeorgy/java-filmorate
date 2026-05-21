package ru.yandex.practicum.filmorate.model;

import lombok.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private long duration;
    private MpaRating mpa;

    @Builder.Default
    private LinkedHashSet<Genre> genres = new LinkedHashSet<>();

    @Builder.Default
    private LinkedHashSet<Director> director = new LinkedHashSet<>();
}
