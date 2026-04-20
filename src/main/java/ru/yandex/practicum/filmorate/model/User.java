package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Map<Integer, FriendshipStatus> friends = new HashMap<>();

    public void addFriend(Integer friendId) {
        if (friends == null) {
            friends = new HashMap<>();
        }
        friends.put(friendId, FriendshipStatus.PENDING);
    }

    public void addFriend(Integer friendId, FriendshipStatus status) {
        if (friends == null) {
            friends = new HashMap<>();
        }
        friends.put(friendId, status);
    }

    public void removeFriend(Integer friendId) {
        if (friends != null) {
            friends.remove(friendId);
        }
    }

    public boolean isFriend(Integer friendId) {
        return friends != null && friends.containsKey(friendId);
    }

    public boolean isFriendConfirmed(Integer friendId) {
        return friends != null && friends.get(friendId) == FriendshipStatus.CONFIRMED;
    }

    public FriendshipStatus getFriendshipStatus(Integer friendId) {
        return friends != null ? friends.get(friendId) : null;
    }

    public void confirmFriendship(Integer friendId) {
        if (friends != null && friends.containsKey(friendId)) {
            friends.put(friendId, FriendshipStatus.CONFIRMED);
        }
    }

    public Set<Integer> getFriendIds() {
        return friends != null ? friends.keySet() : Set.of();
    }

    public Set<Integer> getConfirmedFriendIds() {
        if (friends == null) {
            return Set.of();
        }
        return friends.entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}