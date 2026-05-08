package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Primary
@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final UserRepository userRepository;

    @Override
    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    @Override
    public User create(User user) {
        return userRepository.create(user);
    }

    @Override
    public User update(User user) {
        return userRepository.update(user);
    }

    @Override
    public boolean existsById(Integer id) {
        return userRepository.existsById(id);
    }

    @Override
    public void delete(Integer id) {
        userRepository.deleteById(id);
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        userRepository.addFriend(userId, friendId);
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        userRepository.removeFriend(userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Integer userId) {
        return userRepository.getFriends(userId);
    }

    @Override
    public Collection<User> getCommonFriends(Integer userId, Integer otherId) {
        return userRepository.getCommonFriends(userId, otherId);
    }
}
