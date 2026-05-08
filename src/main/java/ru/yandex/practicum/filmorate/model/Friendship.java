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
    private FriendshipStatus status;

    public Friendship(Integer userId, Integer friendId) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = FriendshipStatus.PENDING;
    }
}
