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

    @NotBlank(message = "Содержимое отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва (положительный/отрицательный) должен быть указан")
    private Boolean isPositive;

    @NotNull(message = "Идентификатор пользователя должен быть указан")
    private Integer userId;

    @NotNull(message = "Идентификатор фильма должен быть указан")
    private Integer filmId;

    private Integer useful = 0;
}