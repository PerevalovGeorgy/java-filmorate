package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("GET-запрос на получение всех пользователей");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDto findById(@PathVariable Integer id) {
        log.info("GET-запрос на получение пользователя с id={}", id);
        return userService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("POST-запрос на создание пользователя: {}", userDto.getLogin());
        return userService.create(userDto);
    }

    @PutMapping
    public UserDto update(@Valid @RequestBody UserDto userDto) {
        log.info("PUT-запрос на обновление пользователя с id={}", userDto.getId());
        return userService.update(userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        log.info("DELETE-запрос: удаление пользователя id={}", id);
        userService.deleteUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("PUT-запрос: Пользователь id={} добавляет в друзья id={}", id, friendId);
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("DELETE-запрос: Пользователь id={} удаляет из друзей id={}", id, friendId);
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<UserDto> getFriends(@PathVariable Integer id) {
        log.info("GET-запрос на получение друзей пользователя с id={}", id);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<UserDto> getCommonFriends(@PathVariable Integer id, @PathVariable @Valid Integer otherId) {
        log.info("GET-запрос на получение общих друзей пользователей id={} и id={}", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}
