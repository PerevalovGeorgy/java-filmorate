package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.Collection;
import java.util.Optional;

public interface MpaRatingStorage {

    Collection<MpaRating> findAll();

    Optional<MpaRating> findById(Integer id);

    boolean existsById(Integer id);
}
