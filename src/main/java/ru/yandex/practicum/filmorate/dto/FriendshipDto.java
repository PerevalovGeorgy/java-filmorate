package ru.yandex.practicum.filmorate.dto;

import lombok.*;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendshipDto {
    private Integer userId;
    private Integer friendId;
    private FriendshipStatus status;
}
