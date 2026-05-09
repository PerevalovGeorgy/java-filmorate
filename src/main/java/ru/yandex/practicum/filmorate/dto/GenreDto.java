package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenreDto {
    @NotNull(message = "ID жанра не может быть пустым")
    private Integer id;

    @NotBlank(message = "Название жанра не может быть пустым")
    private String name;
}
