package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendshipStatusDto {
    @NotNull(message = "ID статуса дружбы не может быть пустым")
    private Integer id;

    @NotBlank(message = "Название статуса дружбы не может быть пустым")
    private String name;
}
