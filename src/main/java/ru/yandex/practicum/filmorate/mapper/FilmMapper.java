package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.LinkedHashSet;

@Component
public class FilmMapper {
    public Film toModel(NewFilmDto dto) {
        return Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(dto.getMpa())
                .genres(dto.getGenres() != null ? dto.getGenres() : new LinkedHashSet<>())
                .build();
    }

    public Film toModel(UpdateFilmDto dto) {
        return Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(dto.getMpa())
                .genres(dto.getGenres() != null ? dto.getGenres() : new LinkedHashSet<>())
                .build();
    }
}
