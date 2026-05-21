package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateFilmDto {
    @NotNull(message = "Id должен быть указан для обновления")
    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private long duration;

    private MpaRatingDto mpa;

    @Builder.Default
    private LinkedHashSet<GenreDto> genres = new LinkedHashSet<>();

    @Builder.Default
    private LinkedHashSet<DirectorDto> director = new LinkedHashSet<>();
}
