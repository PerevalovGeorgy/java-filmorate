package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmMapper {
    private final MpaRatingMapper mpaRatingMapper;
    private final GenreMapper genreMapper;

    public FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }

        LinkedHashSet<GenreDto> genreDtos = film.getGenres() == null
                ? new LinkedHashSet<>()
                : film.getGenres().stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(mpaRatingMapper.toDto(film.getMpa()))
                .genres(genreDtos)
                .build();
    }

    public Film toModel(NewFilmDto dto) {
        if (dto == null) return null;

        var genres = dto.getGenres() == null
                ? new LinkedHashSet<ru.yandex.practicum.filmorate.model.Genre>()
                : dto.getGenres().stream()
                .map(genreMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(mpaRatingMapper.toModel(dto.getMpa()))
                .genres(genres)
                .build();
    }

    public Film toModel(UpdateFilmDto dto) {
        if (dto == null) return null;

        var genres = dto.getGenres() == null
                ? new LinkedHashSet<ru.yandex.practicum.filmorate.model.Genre>()
                : dto.getGenres().stream()
                .map(genreMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(mpaRatingMapper.toModel(dto.getMpa()))
                .genres(genres)
                .build();
    }
}
