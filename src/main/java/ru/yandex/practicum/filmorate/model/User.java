package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Integer id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;

    @Builder.Default
    private Set<Integer> friends = new HashSet<>();

    public void addFriend(Integer friendId) {
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add(friendId);
    }

    public void removeFriend(Integer friendId) {
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    public boolean isFriend(Integer friendId) {
        return friends != null && friends.contains(friendId);
    }
}