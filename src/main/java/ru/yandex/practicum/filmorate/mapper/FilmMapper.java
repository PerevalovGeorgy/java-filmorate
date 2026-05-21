package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmMapper {
    private final MpaRatingMapper mpaRatingMapper;
    private final GenreMapper genreMapper;
    private final DirectorMapper directorMapper;

    public FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }

        LinkedHashSet<GenreDto> genreDtos = film.getGenres() == null
                ? new LinkedHashSet<>()
                : film.getGenres().stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        LinkedHashSet<DirectorDto> directorDtos = film.getDirector() == null
                ? new LinkedHashSet<>()
                : film.getDirector().stream()
                .map(directorMapper::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(mpaRatingMapper.toDto(film.getMpa()))
                .genres(genreDtos)
                .director(directorDtos)
                .build();
    }

    public Film toModel(NewFilmDto dto) {
        if (dto == null) return null;

        var genres = dto.getGenres() == null
                ? new LinkedHashSet<ru.yandex.practicum.filmorate.model.Genre>()
                : dto.getGenres().stream()
                .map(genreMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var director = dto.getDirector() == null
                ? new LinkedHashSet<ru.yandex.practicum.filmorate.model.Director>()
                : dto.getDirector().stream()
                .map(directorMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(mpaRatingMapper.toModel(dto.getMpa()))
                .genres(genres)
                .director(director)
                .build();
    }

    public Film toModel(UpdateFilmDto dto) {
        if (dto == null) return null;

        var genres = dto.getGenres() == null
                ? new LinkedHashSet<ru.yandex.practicum.filmorate.model.Genre>()
                : dto.getGenres().stream()
                .map(genreMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var director = dto.getDirector() == null
                ? new LinkedHashSet<ru.yandex.practicum.filmorate.model.Director>()
                : dto.getDirector().stream()
                .map(directorMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(mpaRatingMapper.toModel(dto.getMpa()))
                .genres(genres)
                .director(director)
                .build();
    }
}
