package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        Collection<Film> allFilms = films.values();
        log.debug("Найдено фильмов: {}", allFilms.size());
        return allFilms;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.debug("Создание фильма: {}", film);

        validateFilm(film);

        int id = getNextId();
        film.setId(id);
        films.put(id, film);

        log.info("Фильм создан с id: {}", id);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.debug("Обновление фильма: {}", newFilm);

        if (newFilm.getId() == null) {
            log.warn("Id фильма не указан при обновлении");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id = {} не найден при обновлении", newFilm.getId());
            throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        validateFilm(newFilm);

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с id = {} успешно обновлен", newFilm.getId());
        return newFilm;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        log.debug("Название фильма валидно: {}", film.getName());

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание фильма превышает 200 символов. Длина: {}",
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        log.debug("Описание фильма валидно. Длина: {}",
                film.getDescription() != null ? film.getDescription().length() : 0);

        LocalDate minDate = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(minDate)) {
            log.warn("Валидация не пройдена: дата релиза {} раньше {}", film.getReleaseDate(), minDate);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        log.debug("Дата релиза фильма валидна: {}", film.getReleaseDate());

        if (film.getDuration() == null) {
            log.warn("Валидация не пройдена: продолжительность фильма не указана");
            throw new ValidationException("Продолжительность должна быть указана");
        }

        long durationMinutes = film.getDuration().toMinutes();
        if (durationMinutes <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма {} минут должна быть положительной",
                    durationMinutes);
            throw new ValidationException("Продолжительность должна быть положительной");
        }
        log.debug("Продолжительность фильма валидна: {} минут", durationMinutes);
    }

    private int getNextId() {
        int nextId = ++currentId;
        log.debug("Сгенерирован новый id для фильма: {}", nextId);
        return nextId;
    }
}