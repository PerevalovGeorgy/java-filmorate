package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.debug("Запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Integer id) {
        log.debug("Запрос на получение фильма с id={}", id);
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.debug("Создание фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.debug("Обновление фильма: {}", newFilm);
        return filmService.update(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public Set<Integer> setLike(@PathVariable("id") Integer filmId,
                                @PathVariable("userId") Integer userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);
        return filmService.setLikeFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Set<Integer> deleteLike(@PathVariable("id") Integer filmId,
                                   @PathVariable("userId") Integer userId) {
        log.debug("Пользователь {} удаляет лайк у фильма {}", userId, filmId);
        return filmService.deleteLikeFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(required = false) Integer count) {
        log.debug("Запрос на получение популярных фильмов, count={}", count);
        return filmService.getFilmsByLikes(count);
    }
}