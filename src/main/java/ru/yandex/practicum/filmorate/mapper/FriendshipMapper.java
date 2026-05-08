package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FriendshipDto;
import ru.yandex.practicum.filmorate.dto.NewFriendshipDto;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

@Component
public class FriendshipMapper {
    public Friendship toModel(NewFriendshipDto dto) {
        return Friendship.builder()
                .userId(dto.getUserId())
                .friendId(dto.getFriendId())
                .status(FriendshipStatus.PENDING)
                .build();
    }

    public FriendshipDto toDto(Friendship friendship) {
        return FriendshipDto.builder()
                .userId(friendship.getUserId())
                .friendId(friendship.getFriendId())
                .status(friendship.getStatus())
                .build();
    }
}
