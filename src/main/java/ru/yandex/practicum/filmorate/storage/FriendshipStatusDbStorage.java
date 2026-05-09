package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FriendshipStatusRowMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FriendshipStatusDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FriendshipStatusRowMapper statusRowMapper;

    public Collection<FriendshipStatus> findAll() {
        String sql = "SELECT * FROM friendship_statuses ORDER BY id";
        return jdbcTemplate.query(sql, statusRowMapper);
    }

    public Optional<FriendshipStatus> findById(Integer id) {
        String sql = "SELECT * FROM friendship_statuses WHERE id = ?";
        try {
            FriendshipStatus status = jdbcTemplate.queryForObject(sql, statusRowMapper, id);
            return Optional.ofNullable(status);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
