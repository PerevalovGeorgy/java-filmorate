package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaRatingMapper;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingStorage mpaRatingStorage;
    private final MpaRatingMapper mpaRatingMapper;

    public Collection<MpaRatingDto> findAll() {
        log.info("Получен запрос на получение всех рейтингов MPA");
        return mpaRatingStorage.findAll().stream()
                .map(mpaRatingMapper::toDto)
                .collect(Collectors.toList());
    }

    public MpaRatingDto findById(Integer id) {
        log.info("Получен запрос на получение рейтинга MPA с id={}", id);
        return mpaRatingStorage.findById(id)
                .map(mpaRatingMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Рейтинг MPA с id={} не найден", id);
                    return new NotFoundException("Рейтинг MPA с id " + id + " не найден");
                });
    }
}
