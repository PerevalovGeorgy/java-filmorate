package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
    private Integer friendId;
    private FriendshipStatus status;

    public Friendship(Integer friendId) {
        this.friendId = friendId;
        this.status = FriendshipStatus.PENDING;
    }
}
