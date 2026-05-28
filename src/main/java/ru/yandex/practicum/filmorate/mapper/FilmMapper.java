package ru.yandex.practicum.filmorate.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmMapper {
    private final MpaRatingMapper mpaRatingMapper;
    private final GenreMapper genreMapper;
    private final DirectorMapper directorMapper;

    private LinkedHashSet<GenreDto> mapGenresToDto(Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return genres.stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LinkedHashSet<Genre> mapGenresToModel(Collection<GenreDto> genreDtos) {
        if (genreDtos == null || genreDtos.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return genreDtos.stream()
                .map(genreMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LinkedHashSet<DirectorDto> mapDirectorsToDto(Collection<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return directors.stream()
                .map(directorMapper::toDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private LinkedHashSet<Director> mapDirectorsToModel(Collection<DirectorDto> directorDtos) {
        if (directorDtos == null || directorDtos.isEmpty()) {
            return new LinkedHashSet<>();
        }
        return directorDtos.stream()
                .map(directorMapper::toModel)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }

        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(mpaRatingMapper.toDto(film.getMpa()))
                .genres(mapGenresToDto(film.getGenres()))
                .director(mapDirectorsToDto(film.getDirector()))
                .build();
    }

    public Film toModel(NewFilmDto dto) {
        if (dto == null) {
            return null;
        }

        return Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(mpaRatingMapper.toModel(dto.getMpa()))
                .genres(mapGenresToModel(dto.getGenres()))
                .director(mapDirectorsToModel(dto.getDirector()))
                .build();
    }

    public Film toModel(UpdateFilmDto dto) {
        if (dto == null) {
            return null;
        }

        return Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .mpa(mpaRatingMapper.toModel(dto.getMpa()))
                .genres(mapGenresToModel(dto.getGenres()))
                .director(mapDirectorsToModel(dto.getDirector()))
                .build();
    }
}