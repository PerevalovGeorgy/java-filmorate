package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Collection<FilmDto>> findAll() {
        log.debug("GET-запрос на получение всех фильмов");
        return ResponseEntity.ok(filmService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> findById(@PathVariable Integer id) {
        log.debug("GET-запрос на получение фильма с id={}", id);
        return ResponseEntity.ok(filmService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FilmDto> create(@Valid @RequestBody NewFilmDto newFilmDto) {
        log.debug("POST-запрос на создание фильма: {}", newFilmDto.getName());
        FilmDto created = filmService.create(newFilmDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<FilmDto> update(@Valid @RequestBody UpdateFilmDto updateFilmDto) {
        log.debug("PUT-запрос на обновление фильма с id={}", updateFilmDto.getId());
        return ResponseEntity.ok(filmService.update(updateFilmDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable("id") Integer id) {
        log.debug("DELETE-запрос: удаление фильма - {}", id);
        filmService.deleteFilm(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> setLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        log.debug("PUT-запрос: Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmService.setLikeFilm(filmId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable("id") Integer filmId, @PathVariable("userId") Integer userId) {
        log.debug("DELETE-запрос: Пользователь {} удаляет лайк у фильма {}", userId, filmId);
        filmService.deleteLikeFilm(filmId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<Collection<FilmDto>> getPopularFilms(
            @RequestParam(defaultValue = "10") Integer count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {
        log.debug("GET-запрос на получение популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);

        Collection<FilmDto> films;
        if (genreId != null || year != null) {
            films = filmService.getPopularFilmsByGenreAndYear(count, genreId, year);
        } else {
            films = filmService.getFilmsByLikes(count);
        }

        return ResponseEntity.ok(films);
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<Collection<FilmDto>> getFilmsByDirector(
            @PathVariable Integer directorId,
            @RequestParam String sortBy) {
        log.debug("GET-запрос на получение фильмов режиссера с id={}, sortBy={}", directorId, sortBy);
        return ResponseEntity.ok(filmService.getFilmsByDirectorId(directorId, sortBy));
    }

    @GetMapping("/common")
    public ResponseEntity<Collection<FilmDto>> getCommonFilmsByLikes(
            @RequestParam Integer userId,
            @RequestParam Integer friendId) {
        log.debug("GET-запрос на получение общих фильмов двух пользователей с id={}, id={}", userId, friendId);
        return ResponseEntity.ok(filmService.getCommonFilmsOrderByLikes(userId, friendId));
    }

    @GetMapping("/search")
    public ResponseEntity<Collection<FilmDto>> searchFilms(
            @RequestParam String query,
            @RequestParam String by) {
        log.debug("GET-запрос на поиск фильмов: query={}, by={}", query, by);
        return ResponseEntity.ok(filmService.searchFilms(query, by));
    }
}
