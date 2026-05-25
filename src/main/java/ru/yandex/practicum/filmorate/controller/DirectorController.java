package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<Collection<DirectorDto>> findAll() {
        log.info("GET-запрос на получение всех режиссеров");
        return ResponseEntity.ok(directorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDto> findById(@PathVariable Integer id) {
        log.info("GET-запрос на получение режиссера с id={}", id);
        return ResponseEntity.ok(directorService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DirectorDto> create(@Valid @RequestBody DirectorDto directorDto) {
        log.info("POST-запрос на создание режиссера: {}", directorDto);
        DirectorDto created = directorService.create(directorDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<DirectorDto> update(@Valid @RequestBody DirectorDto directorDto) {
        log.info("PUT-запрос на обновление режиссера с id={}", directorDto.getId());
        return ResponseEntity.ok(directorService.update(directorDto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirector(@PathVariable("id") Integer directorId) {
        log.debug("DELETE-запрос на удаление режиссера с id={}", directorId);
        directorService.delete(directorId);
    }
}
