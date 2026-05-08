package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaService {
    private final MpaRepository mpaRepository;

    public Collection<MpaRating> findAll() {
        return mpaRepository.findAll();
    }

    public MpaRating findById(Integer id) {
        return mpaRepository.findById(id)
                .orElseThrow(() -> new MoviePresenceInListException("Рейтинг MPA с id=" + id + " не найден"));
    }
}
