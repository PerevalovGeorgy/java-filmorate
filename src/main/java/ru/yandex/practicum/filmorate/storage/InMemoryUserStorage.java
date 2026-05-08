package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component("inMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 0;

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User create(User user) {
        int id = getNextId();
        user.setId(id);
        users.put(id, user);
        log.debug("Пользователь сохранен в памяти с id: {}", id);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.debug("Пользователь с id = {} обновлен в памяти", user.getId());
        return user;
    }

    @Override
    public boolean existsById(Integer id) {
        return users.containsKey(id);
    }

    @Override
    public void delete(Integer id) {
        if (!users.containsKey(id)) {
            throw new IllegalArgumentException("Пользователь с id = " + id + " не найден для удаления из памяти");
        }
        users.remove(id);
        log.debug("Пользователь с id = {} удален из памяти", id);
    }

    private int getNextId() {
        return ++currentId;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
    }

    @Override
    public Collection<User> getFriends(Integer userId) {
        return java.util.Collections.emptyList();
    }

    @Override
    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        return java.util.Collections.emptyList();
    }
}
