package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<Collection<GenreDto>> findAll() {
        log.info("GET-запрос на получение всех жанров");
        Collection<GenreDto> genreDtos = genreService.findAll();
        return ResponseEntity.ok(genreDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> findById(@PathVariable Integer id) {
        log.info("GET-запрос на получение жанра с id={}", id);
        return ResponseEntity.ok(genreService.findById(id));
    }
}
