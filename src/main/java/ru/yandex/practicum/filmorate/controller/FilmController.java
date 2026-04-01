package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;
    private static final int MAXTEXT = 200;
    private static final LocalDate MINDATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        Collection<Film> allFilms = films.values();
        log.debug("Найдено фильмов: {}", allFilms.size());
        return allFilms;
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Создание фильма: {}", film);

        validateFilm(film);

        int id = getNextId();
        film.setId(id);
        films.put(id, film);

        log.info("Фильм создан с id: {}", id);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
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

        if (film.getDescription() != null && film.getDescription().length() > MAXTEXT) {
            log.warn("Валидация не пройдена: описание фильма превышает 200 символов. Длина: {}",
                    film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        log.debug("Описание фильма валидно. Длина: {}",
                film.getDescription() != null ? film.getDescription().length() : 0);

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MINDATE)) {
            log.warn("Валидация не пройдена: дата релиза {} раньше {}", film.getReleaseDate(), MINDATE);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        log.debug("Дата релиза фильма валидна: {}", film.getReleaseDate());

        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма {} должна быть положительной",
                    film.getDuration());
            throw new ValidationException("Продолжительность должна быть положительной");
        }
        log.debug("Продолжительность фильма валидна: {} минут", film.getDuration());
    }

    private int getNextId() {
        int nextId = ++currentId;
        log.debug("Сгенерирован новый id для фильма: {}", nextId);
        return nextId;
    }
}