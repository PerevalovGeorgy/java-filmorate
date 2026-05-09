package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;
    private final FilmMapper filmMapper;
    private final MpaRatingStorage mpaRatingStorage;
    private final GenreStorage genreStorage;

    private static final LocalDate MINDATE = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmRepository") FilmStorage filmStorage,
                       UserService userService,
                       FilmMapper filmMapper,
                       MpaRatingStorage mpaRatingStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.filmMapper = filmMapper;
        this.mpaRatingStorage = mpaRatingStorage;
        this.genreStorage = genreStorage;
    }

    public Collection<FilmDto> findAll() {
        log.info("Запрос на получение всех фильмов");
        return filmStorage.findAll().stream()
                .map(film -> filmMapper.toDto(film))
                .collect(Collectors.toList());
    }

    public FilmDto findById(Integer id) {
        log.info("Запрос на получение фильма с id={}", id);
        return filmStorage.findById(id)
                .map(film -> filmMapper.toDto(film))
                .orElseThrow(() -> new MoviePresenceInListException("Фильм с id=" + id + " не найден"));
    }

    public FilmDto create(NewFilmDto dto) {
        log.info("Запрос на добавление нового фильма: {}", dto.getName());
        validateFilmDatesAndConstraints(dto.getName(), dto.getReleaseDate(), dto.getDuration());

        if (dto.getMpa() != null && dto.getMpa().getId() != null) {
            mpaRatingStorage.findById(dto.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + dto.getMpa().getId() + " не найден"));
        }

        if (dto.getGenres() != null) {
            dto.getGenres().forEach(genreDto -> {
                if (!genreStorage.existsById(genreDto.getId())) {
                    throw new NotFoundException("Жанр с id " + genreDto.getId() + " не найден");
                }
            });
        }

        Film film = filmMapper.toModel(dto);
        Film createdFilm = filmStorage.create(film);
        return filmMapper.toDto(createdFilm);
    }

    public FilmDto update(UpdateFilmDto dto) {
        log.info("Запрос на обновление фильма с id={}", dto.getId());
        if (dto.getId() == null) {
            log.warn("Id фильма не указан при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        if (!filmStorage.existsById(dto.getId())) {
            log.warn("Фильм с id = {} не найден при обновлении", dto.getId());
            throw new MoviePresenceInListException("Фильм с id = " + dto.getId() + " не найден");
        }
        validateFilmDatesAndConstraints(dto.getName(), dto.getReleaseDate(), dto.getDuration());

        if (dto.getMpa() != null && dto.getMpa().getId() != null) {
            mpaRatingStorage.findById(dto.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + dto.getMpa().getId() + " не найден"));
        }

        if (dto.getGenres() != null) {
            dto.getGenres().forEach(genreDto -> {
                if (!genreStorage.existsById(genreDto.getId())) {
                    throw new NotFoundException("Жанр с id " + genreDto.getId() + " не найден");
                }
            });
        }

        Film film = filmMapper.toModel(dto);
        Film updatedFilm = filmStorage.update(film);
        return filmMapper.toDto(updatedFilm);
    }

    public void setLikeFilm(Integer filmId, Integer userId) {
        log.info("Запрос: лайк фильму id={} от пользователя id={}", filmId, userId);
        if (!filmStorage.existsById(filmId)) {
            throw new MoviePresenceInListException("Такого фильма нет в списке фильмов");
        }
        userService.findById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLikeFilm(Integer filmId, Integer userId) {
        log.info("Запрос: удаление лайка у фильма id={} пользователем id={}", filmId, userId);
        if (!filmStorage.existsById(filmId)) {
            throw new MoviePresenceInListException("Такого фильма нет в списке фильмов");
        }
        userService.findById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<FilmDto> getFilmsByLikes(Integer count) {
        log.info("Запрос на получение популярных фильмов, лимит: {}", count);
        return filmStorage.getPopularFilms(count).stream()
                .map(film -> filmMapper.toDto(film))
                .collect(Collectors.toList());
    }

    private void validateFilmDatesAndConstraints(String name, LocalDate releaseDate, long duration) {
        if (name == null || name.isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (releaseDate != null && releaseDate.isBefore(MINDATE)) {
            log.warn("Валидация не пройдена: дата релиза {} раньше {}", releaseDate, MINDATE);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (duration <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма {} должна быть положительной", duration);
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
