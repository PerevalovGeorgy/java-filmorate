package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateFilmDto {
    @NotNull(message = "ID фильма обязателен для обновления")
    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительной")
    private long duration;

    @NotNull(message = "Рейтинг MPA должен быть указан")
    private MpaRating mpa;

    @Builder.Default
    private LinkedHashSet<Genre> genres = new LinkedHashSet<>();
}
