package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public Collection<MpaRating> findAll() {
        log.debug("Получен запрос на список всех рейтингов MPA");
        return mpaService.findAll();
    }

    @GetMapping("/{id}")
    public MpaRating findById(@PathVariable Integer id) {
        log.debug("Получен запрос на рейтинг MPA с id={}", id);
        return mpaService.findById(id);
    }
}
