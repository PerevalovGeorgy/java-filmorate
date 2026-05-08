package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaRepository {
    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    public Collection<MpaRating> findAll() {
        String sql = "SELECT id FROM MpaRating ORDER BY id ASC";
        return jdbc.query(sql, mpaRowMapper);
    }

    public Optional<MpaRating> findById(Integer id) {
        String sql = "SELECT id FROM MpaRating WHERE id = ?";
        List<MpaRating> mpaRatings = jdbc.query(sql, mpaRowMapper, id);
        return mpaRatings.stream().findFirst();
    }
}
