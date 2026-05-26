package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.RecommendationService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RecommendationService recommendationService;

    @GetMapping
    public ResponseEntity<Collection<UserDto>> findAll() {
        log.info("GET-запрос на получение всех пользователей");
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Integer id) {
        log.info("GET-запрос на получение пользователя с id={}", id);
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        log.info("POST-запрос на создание пользователя: {}", userDto.getLogin());
        UserDto created = userService.create(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<UserDto> update(@Valid @RequestBody UserDto userDto) {
        log.info("PUT-запрос на обновление пользователя с id={}", userDto.getId());
        return ResponseEntity.ok(userService.update(userDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        log.info("DELETE-запрос: удаление пользователя id={}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("PUT-запрос: Пользователь id={} добавляет в друзья id={}", id, friendId);
        userService.addFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable Integer id, @PathVariable Integer friendId) {
        log.info("DELETE-запрос: Пользователь id={} удаляет из друзей id={}", id, friendId);
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Collection<UserDto>> getFriends(@PathVariable Integer id) {
        log.info("GET-запрос на получение друзей пользователя с id={}", id);
        return ResponseEntity.ok(userService.getFriends(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Collection<UserDto>> getCommonFriends(@PathVariable Integer id, @PathVariable Integer otherId) {
        log.info("GET-запрос на получение общих друзей пользователей id={} и id={}", id, otherId);
        return ResponseEntity.ok(userService.getCommonFriends(id, otherId));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<Collection<FilmDto>> getRecommendations(@PathVariable Integer id) {
        log.info("GET-запрос на получение рекомендаций для пользователя id={}", id);
        return ResponseEntity.ok(recommendationService.recommendedFilms(id));
    }

    @GetMapping("/{id}/feed")
    public Collection<Feed> getFeed(@PathVariable Integer id) {
        log.info("GET-запрос на получение ленты новостей пользователя id={}", id);
        return feedService.getFeed(id);
    }
}
