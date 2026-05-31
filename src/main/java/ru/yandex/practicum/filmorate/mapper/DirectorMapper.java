package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

@Component
public class DirectorMapper {

    public DirectorDto toDto(Director director) {
        if (director == null) {
            return null;
        }
        return DirectorDto.builder()
                .id(director.getId())
                .name(director.getName())
                .build();
    }

    public Director toModel(DirectorDto dto) {
        if (dto == null) {
            return null;
        }
        return Director.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
