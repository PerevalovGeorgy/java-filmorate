package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;
    private final UserService userService;
    private final FilmMapper filmMapper;
    private final MpaRepository mpaRepository;
    private final GenreRepository genreRepository;

    private static final LocalDate MINDATE = LocalDate.of(1895, 12, 28);


    public Collection<FilmDto> findAll() {
        log.info("Запрос на получение всех фильмов");
        return filmRepository.findAll().stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto findById(Integer id) {
        log.info("Запрос на получение фильма с id={}", id);
        return filmRepository.findById(id)
                .map(filmMapper::toDto)
                .orElseThrow(() -> new MoviePresenceInListException("Фильм с id=" + id + " не найден"));
    }

    public FilmDto create(NewFilmDto dto) {
        log.info("Запрос на добавление нового фильма: {}", dto.getName());
        validateFilmDatesAndConstraints(dto.getName(), dto.getReleaseDate(), dto.getDuration());

        if (dto.getMpa() != null && dto.getMpa().getId() != null) {
            mpaRepository.findById(dto.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + dto.getMpa().getId() + " не найден"));
        }

        if (dto.getGenres() != null) {
            dto.getGenres().forEach(genreDto -> {
                if (! genreRepository.existsById(genreDto.getId())) {
                    throw new NotFoundException("Жанр с id " + genreDto.getId() + " не найден");
                }
            });
        }

        if (dto.getDirector() != null) {
            dto.getDirector().forEach(directorDto -> {
                if (! directorRepository.existsById(directorDto.getId())) {
                    throw new NotFoundException("Режиссер с id " + directorDto.getId() + " не найден");
                }
            });
        }

        Film film = filmMapper.toModel(dto);
        Film createdFilm = filmRepository.create(film);
        return filmMapper.toDto(createdFilm);
    }

    public void deleteFilm(Integer id) {
        log.info("Запрос на удаление фильма с id - {}", id);
        findById(id);
        filmRepository.deleteFilm(id);
    }

    public FilmDto update(UpdateFilmDto dto) {
        log.info("Запрос на обновление фильма с id={}", dto.getId());
        if (dto.getId() == null) {
            log.warn("Id фильма не указан при обновлении");
            throw new ValidationException("Id должен быть указан");
        }
        if (! filmRepository.existsById(dto.getId())) {
            log.warn("Фильм с id = {} не найден при обновлении", dto.getId());
            throw new MoviePresenceInListException("Фильм с id = " + dto.getId() + " не найден");
        }
        validateFilmDatesAndConstraints(dto.getName(), dto.getReleaseDate(), dto.getDuration());

        if (dto.getMpa() != null && dto.getMpa().getId() != null) {
            mpaRepository.findById(dto.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id " + dto.getMpa().getId() + " не найден"));
        }

        if (dto.getGenres() != null) {
            dto.getGenres().forEach(genreDto -> {
                if (! genreRepository.existsById(genreDto.getId())) {
                    throw new NotFoundException("Жанр с id " + genreDto.getId() + " не найден");
                }
            });
        }

        if (dto.getDirector() != null) {
            dto.getDirector().forEach(directorDto -> {
                if (! directorRepository.existsById(directorDto.getId())) {
                    throw new NotFoundException("Режиссер с id " + directorDto.getId() + " не найден");
                }
            });
        }

        Film film = filmMapper.toModel(dto);
        Film updatedFilm = filmRepository.update(film);
        return filmMapper.toDto(updatedFilm);
    }

    public void setLikeFilm(Integer filmId, Integer userId) {
        log.info("Запрос: лайк фильму id={} от пользователя id={}", filmId, userId);
        if (! filmRepository.existsById(filmId)) {
            throw new MoviePresenceInListException("Такого фильма нет в списке фильмов");
        }
        userService.findById(userId);
        filmRepository.addLike(filmId, userId);
    }

    public void deleteLikeFilm(Integer filmId, Integer userId) {
        log.info("Запрос: удаление лайка у фильма id={} пользователем id={}", filmId, userId);
        if (! filmRepository.existsById(filmId)) {
            throw new MoviePresenceInListException("Такого фильма нет в списке фильмов");
        }
        userService.findById(userId);
        filmRepository.removeLike(filmId, userId);
    }

    public Collection<FilmDto> getFilmsByLikes(Integer count) {
        log.info("Запрос на получение популярных фильмов, лимит: {}", count);
        return filmRepository.getPopularFilms(count).stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public Collection<FilmDto> getCommonFilmsOrderByLikes(Integer userId, Integer friendId) {
        log.info("Запрос на получение общих фильмов двух пользователей, user1 c id = {} и user2 c id = {}", userId, friendId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!userRepository.existsById(friendId)) {
            throw new NotFoundException("Пользователь (друг) с id = " + friendId + " не найден");
        }
        return filmRepository.getCommonFilms(userId, friendId).stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public Collection<FilmDto> getFilmsByDirectorId(Integer directorId, String sortBy) {
        log.info("Запрос на получение фильмов режиссера с id={} с сортировкой по: {}", directorId, sortBy);
        if (! directorRepository.existsById(directorId)) {
            throw new NotFoundException("Режиссер с id " + directorId + " не найден");
        }
        return filmRepository.getFilmsByDirectorId(directorId, sortBy).stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    protected Collection<FilmDto> getFilmsOnlyUserLikes(Integer user1Id, Integer user2Id) {
        Collection<Film> user1Films = filmRepository.getLikedFilmsByUser(user1Id);
        Collection<Film> commonFilms = filmRepository.getLikedFilmsByUser(user2Id);
        if (user1Films == null || commonFilms == null) {
            throw new NotFoundException("Значение не может быть null");
        }
        return user1Films.stream()
                .filter(Predicate.not(commonFilms::contains))
    public Collection<FilmDto> getPopularFilmsByGenreAndYear(Integer count, Integer genreId, Integer year) {
        log.info("Запрос на получение популярных фильмов по жанру с id={} за год={}, лимит: {}", genreId, year, count);

        if (year != null) {
            int minYear = MINDATE.getYear();
            int currentYear = LocalDate.now().getYear();
            if (year < minYear || year > currentYear) {
                log.warn("Валидация не пройдена: указанный год {} вне допустимого диапазона [{}-{}]", year, minYear,
                        currentYear);
                throw new ValidationException("Год должен быть не раньше " + minYear + " и не в будущем (текущий год: "
                        + currentYear + ")");
            }
        }

        if (genreId != null) {
            if (! genreRepository.existsById(genreId)) {
                log.warn("Валидация не пройдена: жанр с id={} не найден", genreId);
                throw new NotFoundException("Жанр с id " + genreId + " не найден");
            }
        }

        Collection<Film> films;
        if (genreId != null && year != null) {
            films = filmRepository.getPopularFilmsByGenreAndYear(count, genreId, year);
        } else if (genreId != null) {
            films = filmRepository.getPopularFilmsByGenre(count, genreId);
        } else if (year != null) {
            films = filmRepository.getPopularFilmsByYear(count, year);
        } else {
            films = filmRepository.getPopularFilms(count);
        }

        return films.stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }


    private void validateFilmDatesAndConstraints(String name, LocalDate releaseDate, long duration) {
        if (name == null || name.isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (releaseDate != null && releaseDate.isBefore(MINDATE)) {
            log.warn("Валидация не пройдена: дата релиза {} раньше {}", releaseDate, MINDATE);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (duration <= 0) {
            log.warn("Валидация не пройдена: продолжительность фильма {} должна быть положительной", duration);
            throw new ValidationException("Продолжительность должна быть положительной");
        }
    }
}
