package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    private static final int MAXTEXT = 200;
    private static final LocalDate MINDATE = LocalDate.of(1895, 12, 28);
    private static final int DEFAULT_POPULAR_COUNT = 10;


    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Integer id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new MoviePresenceInListException("Фильм с id=" + id + " не найден"));
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Id фильма не указан при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        if (!filmStorage.existsById(newFilm.getId())) {
            log.warn("Фильм с id = {} не найден при обновлении", newFilm.getId());
            throw new MoviePresenceInListException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        validateFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public void setLikeFilm(Integer filmId, Integer userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new MoviePresenceInListException("Такого фильма нет в списке фильмов");
        }
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLikeFilm(Integer filmId, Integer userId) {
        if (!filmStorage.existsById(filmId)) {
            throw new MoviePresenceInListException("Такого фильма нет в списке фильмов");
        }
        if (!userService.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public Collection<Film> getFilmsByLikes(Integer count) {
        int limit = (count != null && count > 0) ? count : DEFAULT_POPULAR_COUNT;
        return filmStorage.getPopularFilms(limit);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название не может бытьqdm пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAXTEXT) {
            log.warn("Валидация не пройдена: описание фильма превышает {} символов", MAXTEXT);
            throw new ValidationException("Максимальная длина описания — " + MAXTEXT + " символов");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MINDATE)) {
            log.warn("Валидация не пройдена: дата релиза {} раньше {}", film.getReleaseDate(), MINDATE);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма {} должна быть положительной", film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
