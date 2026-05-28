package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    public Collection<GenreDto> findAll() {
        log.info("Получен запрос на получение всех жанров");
        return genreRepository.findAll().stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toList());
    }

    public GenreDto findById(Integer id) {
        log.info("Получен запрос на получение жанра с id={}", id);
        return genreRepository.findById(id)
                .map(genreMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Жанр с id={} не найден", id);
                    return new NotFoundException("Жанр с id " + id + " не найден");
                });
    }

    public void validateGenresByIds(Collection<GenreDto> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        Set<Integer> requestedIds = genres.stream()
                .map(GenreDto::getId)
                .collect(Collectors.toSet());

        Set<Integer> existingIds = genreRepository.findExistingIds(requestedIds);

        if (existingIds.size() != requestedIds.size()) {
            Set<Integer> notFoundIds = new HashSet<>(requestedIds);
            notFoundIds.removeAll(existingIds);
            throw new NotFoundException("Жанры с ID " + notFoundIds + " не найдены");
        }
    }
}
