package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FeedService feedService;

    public Collection<UserDto> findAll() {
        log.info("Запрос на получение списка всех пользователей");
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto findById(Integer id) {
        log.info("Запрос на получение пользователя с id={}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
    }

    public UserDto create(UserDto userDto) {
        log.info("Запрос на создание пользователя с логином {}", userDto.getLogin());
        User user = userMapper.toModel(userDto);
        User createdUser = userRepository.create(user);
        return userMapper.toDto(createdUser);
    }

    public UserDto update(UserDto userDto) {
        log.info("Запрос на обновление пользователя с id={}", userDto.getId());
        if (userDto.getId() == null || !userRepository.existsById(userDto.getId())) {
            log.warn("Попытка обновления несуществующего пользователя с id={}", userDto.getId());
            throw new NotFoundException("Пользователь с id " + userDto.getId() + " не найден");
        }
        User user = userMapper.toModel(userDto);
        User updatedUser = userRepository.update(user);
        return userMapper.toDto(updatedUser);
    }

    public void deleteUser(Integer id) {
        log.info("Запрос на удаление пользователя с id - {}", id);
        findById(id);
        userRepository.deleteById(id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        log.info("Запрос: добавление в друзья. К кому: id={}, кто: id={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);
        userRepository.addFriend(userId, friendId);
        feedService.logEvent(userId, "FRIEND", "ADD", friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Запрос: удаление из друзей. У кого: id={}, кого: id={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);
        userRepository.removeFriend(userId, friendId);
        feedService.logEvent(userId, "FRIEND", "REMOVE", friendId);
    }

    public void confirmFriendship(Integer userId, Integer friendId) {
        log.info("Запрос: подтверждение дружбы. От кого: id={}, кому: id={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);
        userRepository.confirmFriendship(userId, friendId);
        feedService.logEvent(userId, "FRIEND", "UPDATE", friendId);
    }

    public Collection<UserDto> getFriends(Integer userId) {
        log.info("Запрос на получение списка друзей пользователя с id={}", userId);
        checkUserExists(userId);
        return userRepository.getFriends(userId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public Collection<UserDto> getCommonFriends(Integer userId, Integer otherId) {
        log.info("Запрос на получение общих друзей для id={} и id={}", userId, otherId);
        checkUserExists(userId);
        checkUserExists(otherId);
        return userRepository.getCommonFriends(userId, otherId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public void checkUserExists(Integer id) {
        if (!userRepository.existsById(id)) {
            log.warn("Пользователь с id={} не существует", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}
