package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewFriendshipDto {
    @NotNull(message = "ID пользователя обязателен")
    private Integer userId;

    @NotNull(message = "ID друга обязателен")
    private Integer friendId;
}
