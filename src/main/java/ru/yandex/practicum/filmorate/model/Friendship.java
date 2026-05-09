package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Friendship {
    private Integer userId;
    private Integer friendId;

    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.builder()
            .id(1)
            .name("PENDING")
            .build();
}
