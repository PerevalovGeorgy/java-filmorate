package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.NewUserDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserDto;
import ru.yandex.practicum.filmorate.model.User;

@Component
public class UserMapper {
    public User toModel(NewUserDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .login(dto.getLogin())
                .name(dto.getName() == null || dto.getName().isBlank() ? dto.getLogin() : dto.getName())
                .birthday(dto.getBirthday())
                .build();
    }

    public User toModel(UpdateUserDto dto) {
        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .login(dto.getLogin())
                .name(dto.getName() == null || dto.getName().isBlank() ? dto.getLogin() : dto.getName())
                .birthday(dto.getBirthday())
                .build();
    }
}
