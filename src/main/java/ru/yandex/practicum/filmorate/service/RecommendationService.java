package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Comparator;
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
        Collection<Film> commonFilms = filmRepository.getLikedFilmsByUser(user2Id);
        if (user1Films == null || commonFilms == null) {
            throw new NotFoundException("Значение не может быть null");
        }
        return user1Films.stream()
                .filter(Predicate.not(commonFilms::contains))
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public Collection<FilmDto> recommendedFilms(Integer userId) {
        UserDto mostSimilarUser = findUsersWithMaxLikeOverlap(userId).stream()
                .findFirst().orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return getFilmsOnlyUserLikes(userId, mostSimilarUser.getId());
    }

    protected Collection<UserDto> findUsersWithMaxLikeOverlap(Integer id) {
        Collection<Film> likedFilms = filmRepository.getLikedFilmsByUser(id);
        if (likedFilms.isEmpty()) {
            throw new ValidationException("у пользователя с id - " + id + " нет понравившихся фильмов");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
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
