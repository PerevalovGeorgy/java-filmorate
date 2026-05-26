package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FeedRowMapper;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;

@Repository
public class FeedRepository extends BaseRepository<Feed> {

    public FeedRepository(JdbcTemplate jdbc, FeedRowMapper mapper) {
        super(jdbc, mapper);
    }

    public void addEvent(Feed feed) {
        String sql = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        jdbc.update(sql,
                feed.getTimestamp(),
                feed.getUserId(),
                feed.getEventType(),
                feed.getOperation(),
                feed.getEntityId()
        );
    }

    public Collection<Feed> getFeedByUserId(Integer userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY event_id ASC";
        return findMany(sql, userId);
    }
}
