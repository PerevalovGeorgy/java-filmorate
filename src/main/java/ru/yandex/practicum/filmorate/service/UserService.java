package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Integer id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public User create(User user) {
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено как логин: {}", user.getLogin());
        }

        return userStorage.create(user);
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.warn("Id пользователя не указан при обновлении");
            throw new ValidationException("Id должен быть указан");
        }

        if (!userStorage.existsById(newUser.getId())) {
            log.warn("Пользователь с id = {} не найден при обновлении", newUser.getId());
            throw new UserNotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        validateUser(newUser);

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
            log.debug("Имя пользователя установлено как логин: {}", newUser.getLogin());
        }

        return userStorage.update(newUser);
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        if (user.getId().equals(friend.getId())) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        user.addFriend(friendId);
        friend.addFriend(userId);

        log.info("Пользователь {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(userId);

        log.info("Пользователь {} удален из друзей пользователя {}", friendId, userId);
    }

    public Collection<User> getFriends(Integer userId) {
        User user = findById(userId);

        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = findById(userId);
        User other = findById(otherId);

        Set<Integer> commonFriendsIds = new HashSet<>(user.getFriends());
        commonFriendsIds.retainAll(other.getFriends());

        return commonFriendsIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public boolean existsById(Integer id) {
        return userStorage.existsById(id);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: email пустой");
            throw new ValidationException("Email не может быть пустым");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: email не содержит @ - {}", user.getEmail());
            throw new ValidationException("Email должен содержать @");
        }

        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            log.warn("Валидация не пройдена: некорректный email - {}", user.getEmail());
            throw new ValidationException("Email должен быть корректным (например: user@example.com)");
        }
        log.debug("Email пользователя валиден: {}", user.getEmail());

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: логин пустой");
            throw new ValidationException("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин содержит пробелы - {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        log.debug("Логин пользователя валиден: {}", user.getLogin());

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения в будущем - {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Дата рождения пользователя валидна: {}", user.getBirthday());
    }
}