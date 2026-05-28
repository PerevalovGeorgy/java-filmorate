package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreRepository {
    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;



    public Collection<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id ASC ";
        return jdbc.query(sql, genreRowMapper);
    }

    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<Genre> genres = jdbc.query(sql, genreRowMapper, id);
        return genres.stream().findFirst();
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public Set<Integer> findExistingIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }

        String sql = "SELECT id FROM genres WHERE id IN (" +
                ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";

        List<Integer> existingIds = jdbc.queryForList(sql, Integer.class);
        return new HashSet<>(existingIds);
    }
}
