package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    @GetMapping
    public Collection<MpaRatingDto> findAll() {
        log.info("GET-запрос на получение всех рейтингов MPA");
        return mpaRatingService.findAll();
    }

    @GetMapping("/{id}")
    public MpaRatingDto findById(@PathVariable Integer id) {
        log.info("GET-запрос на получение рейтинга MPA с id={}", id);
        return mpaRatingService.findById(id);
    }
}
