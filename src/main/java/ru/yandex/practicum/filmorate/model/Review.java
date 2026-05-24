package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@EqualsAndHashCode(of = { "reviewId" })
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Integer reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва (isPositive) не может быть null")
    private Boolean isPositive;

    @NotNull(message = "ID пользователя не может быть null")
    private Integer userId;

    @NotNull(message = "ID фильма не может быть null")
    private Integer filmId;

    private Integer useful = 0;
}