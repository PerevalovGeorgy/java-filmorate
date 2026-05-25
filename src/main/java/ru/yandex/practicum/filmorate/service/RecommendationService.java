package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class RecommendationService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final FilmMapper filmMapper;
    private final UserMapper userMapper;

    protected Collection<FilmDto> getFilmsOnlyUserLikes(Integer user1Id, Integer user2Id) {
        Collection<Film> user1Films = filmRepository.getLikedFilmsByUser(user1Id);
        Collection<Film> user2Films = filmRepository.getLikedFilmsByUser(user2Id);
        if (user1Films == null || user2Films == null) {
            return Collections.emptyList();
        }
        if (user1Films.isEmpty() || user2Films.isEmpty()) {
            if (user2Films.isEmpty() && !user1Films.isEmpty()) {
                return user1Films.stream()
                        .map(filmMapper::toDto)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        Set<Film> user2Set = new HashSet<>(user2Films);

        return user1Films.stream()
                .filter(Predicate.not(user2Set::contains))
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public Collection<FilmDto> recommendedFilms(Integer userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            return Collections.emptyList();
        }
        Optional<UserDto> mostSimilarUser = findUsersWithMaxLikeOverlap(userId).stream()
                .findFirst();
        if (mostSimilarUser.isEmpty()) {
            return Collections.emptyList();
        }
        return getFilmsOnlyUserLikes(userId, mostSimilarUser.get().getId());
    }

    protected Collection<UserDto> findUsersWithMaxLikeOverlap(Integer id) {
        Collection<Film> likedFilms = filmRepository.getLikedFilmsByUser(id);
        if (likedFilms == null || likedFilms.isEmpty()) {
            log.info("Для пользователя {} не найдено похожих пользователей", id);
            return Collections.emptyList();
        }
        if (!userRepository.existsById(id)) {
            return Collections.emptyList();
        }

        return userRepository.findAll().stream()
                .filter(user1 -> !user1.getId().equals(id))
                .sorted(Comparator.comparing(
                        user1 -> filmRepository.getLikedFilmsByUser(user1.getId()).stream()
                                .filter(likedFilms::contains)
                                .count(), Comparator.reverseOrder()
                ))
                .map(userMapper::toDto)
                .toList();
    }
}
