package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository genreRepository;

    public Collection<Genre> findAll() {
        return genreRepository.findAll();
    }

    public Genre findById(Integer id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new MoviePresenceInListException("Жанр с id=" + id + " не найден"));
    }
}
