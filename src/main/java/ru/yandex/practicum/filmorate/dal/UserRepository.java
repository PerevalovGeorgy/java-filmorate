package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {

    public UserRepository(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<User> findAll() {
        return findMany("SELECT * FROM users");
    }

    public Optional<User> findById(Integer id) {
        return findOne("SELECT * FROM users WHERE id = ?", id);
    }

    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        int id = insert(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        user.setId(id);
        return user;
    }

    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    public boolean existsById(Integer id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Integer id) {
        delete("DELETE FROM users WHERE id = ?", id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        update("INSERT INTO friendships (user_id, friend_id, friendship_status_id) VALUES (?, ?, 1)", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);
    }

    public Collection<User> getFriends(Integer userId) {
        String sql = "SELECT u.* FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
        return findMany(sql, userId);
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id " +
                "JOIN friendships f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return findMany(sql, userId, otherId);
    }
}
