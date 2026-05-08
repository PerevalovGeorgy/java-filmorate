package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final FilmMapper filmMapper;

    public FilmController(FilmService filmService, FilmMapper filmMapper) {
        this.filmService = filmService;
        this.filmMapper = filmMapper;
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
    public Film create(@Valid @RequestBody NewFilmDto newFilmDto) {
        log.debug("Создание фильма из DTO: {}", newFilmDto);
        Film film = filmMapper.toModel(newFilmDto);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody UpdateFilmDto updateFilmDto) {
        log.debug("Обновление фильма из DTO: {}", updateFilmDto);
        Film film = filmMapper.toModel(updateFilmDto);
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void setLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        log.debug("Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmService.setLikeFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        log.debug("Пользователь {} удаляет лайк у фильма {}", userId, filmId);
        filmService.deleteLikeFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(required = false) Integer count) {
        log.debug("Запрос на получение популярных фильмов, count={}", count);
        return filmService.getFilmsByLikes(count);
    }
}
