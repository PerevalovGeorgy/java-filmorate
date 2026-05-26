package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<FilmDto> findAll() {
        log.debug("GET-запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable Integer id) {
        log.debug("GET-запрос на получение фильма с id={}", id);
        return filmService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FilmDto create(@Valid @RequestBody NewFilmDto newFilmDto) {
        log.debug("POST-запрос на создание фильма: {}", newFilmDto.getName());
        return filmService.create(newFilmDto);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmDto updateFilmDto) {
        log.debug("PUT-запрос на обновление фильма с id={}", updateFilmDto.getId());
        return filmService.update(updateFilmDto);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable("id") Integer id) {
        log.debug("DELETE-запрос: удаление фильма - {}", id);
        filmService.deleteFilm(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void setLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        log.debug("PUT-запрос: Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmService.setLikeFilm(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        log.debug("DELETE-запрос: Пользователь {} удаляет лайк у фильма {}", userId, filmId);
        filmService.deleteLikeFilm(filmId, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        log.debug("GET-запрос на получение популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);

        if (genreId != null || year != null) {
            return filmService.getPopularFilmsByGenreAndYear(count, genreId, year);
        }

        return filmService.getFilmsByLikes(count);
    }

    @GetMapping("/director/{directorId}")
    public Collection<FilmDto> getFilmsByDirector(@PathVariable Integer directorId, @RequestParam String sortBy) {
        log.debug("\"GET-запрос на получение фильмов режиссера с id={}, sortBy={}", directorId, sortBy);
        return filmService.getFilmsByDirectorId(directorId, sortBy);
    }

    @GetMapping("/common")
    public Collection<FilmDto> getCommonFilmsByLikes(@RequestParam Integer userId, @RequestParam Integer friendId) {
        log.debug("\"GET-запрос на получение общих фильмов  двух пользователей с id={}, id={}", userId, friendId);
        return filmService.getCommonFilmsOrderByLikes(userId, friendId);
    }

    @GetMapping("/search")
    public Collection<FilmDto> searchFilms(
            @RequestParam String query,
            @RequestParam String by) {
        log.debug("GET-запрос на поиск фильмов. query={}, by={}", query, by);
        return filmService.searchFilms(query, by);
    }

}
