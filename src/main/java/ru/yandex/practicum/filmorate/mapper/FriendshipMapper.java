package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FriendshipDto;
import ru.yandex.practicum.filmorate.dto.NewFriendshipDto;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

@Component
public class FriendshipMapper {

    public Friendship toModel(NewFriendshipDto dto) {
        if (dto == null) {
            return null;
        }

        FriendshipStatus pendingStatus = FriendshipStatus.builder()
                .id(1)
                .name("PENDING")
                .build();

        return Friendship.builder()
                .userId(dto.getUserId())
                .friendId(dto.getFriendId())
                .status(pendingStatus)
                .build();
    }

    public FriendshipDto toDto(Friendship friendship) {
        if (friendship == null) {
            return null;
        }

        return FriendshipDto.builder()
                .userId(friendship.getUserId())
                .friendId(friendship.getFriendId())
                .status(friendship.getStatus())
                .build();
    }
}
