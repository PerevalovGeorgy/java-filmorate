package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public Collection<DirectorDto> findAll() {
        log.info("GET-запрос на получение всех режиссеров");
        return directorService.findAll();
    }

    @GetMapping("/{id}")
    public DirectorDto findById(@PathVariable Integer id) {
        log.info("GET-запрос на получение режиссера с id={}", id);
        return directorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DirectorDto create(@Valid @RequestBody DirectorDto directorDto) {
        log.info("POST-запрос на создание режиссера: {}", directorDto);
        return directorService.create(directorDto);
    }

    @PutMapping
    public DirectorDto update(@Valid @RequestBody DirectorDto directorDto) {
        log.info("PUT-запрос на обновление режиссера с id={}", directorDto.getId());
        return directorService.update(directorDto);
    }

    @DeleteMapping("/{id}")
    public DirectorDto deleteDirector(@PathVariable("id") Integer directorId) {
        log.debug("DELETE-запрос на удаление режиссера с id={}", directorId);
        return directorService.delete(directorId);
    }
}
