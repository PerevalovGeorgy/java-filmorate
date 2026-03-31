package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 0;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @GetMapping
    public Collection<User> findAll() {
        log.debug("Получение всех пользователей");
        Collection<User> allUsers = users.values();
        log.debug("Найдено пользователей: {}", allUsers.size());
        return allUsers;
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.debug("Создание пользователя: {}", user);

        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя установлено как логин: {}", user.getLogin());
        }

        int id = getNextId();
        user.setId(id);
        users.put(id, user);

        log.info("Пользователь создан с id: {}", id);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.debug("Обновление пользователя: {}", newUser);

        if (newUser.getId() == null) {
            log.warn("Id пользователя не указан при обновлении");
            throw new ValidationException("Id должен быть указан");
        }

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с id = {} не найден при обновлении", newUser.getId());
            throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
        }

        validateUser(newUser);

        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
            log.debug("Имя пользователя установлено как логин: {}", newUser.getLogin());
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id = {} успешно обновлен", newUser.getId());
        return newUser;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Валидация не пройдена: некорректный email - {}", user.getEmail());
            throw new ValidationException("Email должен содержать @ и не быть пустым");
        }
        log.debug("Email пользователя валиден: {}", user.getEmail());

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

    private int getNextId() {
        int nextId = ++currentId;
        log.debug("Сгенерирован новый id: {}", nextId);
        return nextId;
    }
}