package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
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
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new UserNotFoundException("Один из пользователей не найден");
        }
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} отправил запрос на добавление в друзья пользователю {}", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(friendId)) {
            throw new UserNotFoundException("Один из пользователей не найден");
        }
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователь {} удалил из своего списка друзей пользователя {}", userId, friendId);
    }

    public Collection<User> getFriends(Integer userId) {
        if (!userStorage.existsById(userId)) {
            throw new UserNotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        if (!userStorage.existsById(userId) || !userStorage.existsById(otherId)) {
            throw new UserNotFoundException("Один из пользователей не найден");
        }
        return userStorage.getCommonFriends(userId, otherId);
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
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: логин пустой");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин содержит пробелы - {}", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения в будущем - {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
