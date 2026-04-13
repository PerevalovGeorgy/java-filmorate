package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        int id = getNextId();
        film.setId(id);
        films.put(id, film);
        log.debug("Фильм создан с id: {}", id);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        log.debug("Фильм с id = {} обновлен", film.getId());
        return film;
    }

    @Override
    public boolean existsById(Integer id) {
        return films.containsKey(id);
    }

    private int getNextId() {
        return ++currentId;
    }
}