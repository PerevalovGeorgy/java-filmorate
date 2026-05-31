package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Component
public final class UserMapper {

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    public User toModel(UserDto dto) {
        if (dto == null) {
            return null;
        }

        String finalName = (dto.getName() == null || dto.getName().isBlank())
                ? dto.getLogin()
                : dto.getName();

        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .login(dto.getLogin())
                .name(finalName)
                .birthday(dto.getBirthday())
                .build();
    }
}
