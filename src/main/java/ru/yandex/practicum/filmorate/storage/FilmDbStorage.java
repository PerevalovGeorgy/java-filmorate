package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Primary
@Component("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final FilmRepository filmRepository;

    @Override
    public Collection<Film> findAll() {
        return filmRepository.findAll();
    }

    @Override
    public Optional<Film> findById(Integer id) {
        return filmRepository.findById(id);
    }

    @Override
    public Film create(Film film) {
        return filmRepository.create(film);
    }

    @Override
    public Film update(Film film) {
        return filmRepository.update(film);
    }

    @Override
    public boolean existsById(Integer id) {
        return filmRepository.existsById(id);
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        filmRepository.addLike(filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        filmRepository.removeLike(filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return filmRepository.getPopular(count);
    }
}
