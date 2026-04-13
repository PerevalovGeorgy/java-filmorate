package ru.yandex.practicum.filmorate.exception;

public class UserPresenceInListException extends RuntimeException{
    public UserPresenceInListException(String message) {
        super(message);
    }
}
