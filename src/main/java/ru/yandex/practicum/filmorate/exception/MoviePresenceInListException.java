package ru.yandex.practicum.filmorate.exception;

public class MoviePresenceInListException extends RuntimeException {
    public MoviePresenceInListException(String message) {
        super(message);
    }
}
