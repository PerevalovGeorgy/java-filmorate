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
import ru.yandex.practicum.filmorate.model.User;

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

    public Collection<FilmDto> recommendedFilms(Integer userId) {
        log.info("Вызван метод по рекомендации фильма по id={}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("Пользователь с id {} не найден", userId);
            return Collections.emptyList();
        }
        Collection<UserDto> similarUsers = findUsersWithMaxLikeOverlap(userId);
        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        Set<FilmDto> allRecommendations = new LinkedHashSet<>();
        for (UserDto similarUser : similarUsers) {
            allRecommendations.addAll(getFilmsOnlyUserLikes(similarUser.getId(), userId));
        }

        return new ArrayList<>(allRecommendations);
    }

    protected Collection<FilmDto> getFilmsOnlyUserLikes(Integer user1Id, Integer user2Id) {
        Collection<Film> user1Films = filmRepository.getLikedFilmsByUser(user1Id);
        Collection<Film> user2Films = filmRepository.getLikedFilmsByUser(user2Id);
        if (user1Films == null || user1Films.isEmpty()) {
            return Collections.emptyList();
        }

        if (user2Films == null || user2Films.isEmpty()) {
            return user1Films.stream()
                    .map(filmMapper::toDto)
                    .collect(Collectors.toList());
        }

        Set<Film> user2Set = new HashSet<>(user2Films);

        return user1Films.stream()
                .filter(Predicate.not(user2Set::contains))
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    protected Collection<UserDto> findUsersWithMaxLikeOverlap(Integer id) {
        log.info("Использован метод на получение максимально похожих пользователей по id={}", id);
        if (!userRepository.existsById(id)) {
            return Collections.emptyList();
        }

        Set<Integer> likedFilmIds = filmRepository.getLikedFilmsByUser(id).stream()
                .map(Film::getId)
                .collect(Collectors.toSet());

        if (likedFilmIds.isEmpty()) {
            log.info("Для пользователя {} не найдено похожих пользователей", id);
            return Collections.emptyList();
        }

        Collection<User> allUsers = userRepository.findAll();
        List<Integer> otherUserIds = allUsers.stream()
                .filter(u -> !u.getId().equals(id))
                .map(User::getId)
                .collect(Collectors.toList());

        Map<Integer, Set<Integer>> userFilmsMap = filmRepository.getLikedFilmIdsGroupedByUsers(otherUserIds);

        return allUsers.stream()
                .filter(u -> !u.getId().equals(id))
                .filter(u -> {
                    Set<Integer> userFilms = userFilmsMap.getOrDefault(u.getId(), Set.of());
                    return userFilms.stream().anyMatch(likedFilmIds::contains);
                })
                .sorted((u1, u2) -> {
                    long count1 = userFilmsMap.getOrDefault(u1.getId(), Set.of())
                            .stream().filter(likedFilmIds::contains).count();
                    long count2 = userFilmsMap.getOrDefault(u2.getId(), Set.of())
                            .stream().filter(likedFilmIds::contains).count();
                    return Long.compare(count2, count1);
                })
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }
}
