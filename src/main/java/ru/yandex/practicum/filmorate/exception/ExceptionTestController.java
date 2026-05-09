package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class ExceptionTestController {

    @GetMapping("/test/validation")
    public void throwValidation() {
        throw new ru.yandex.practicum.filmorate.exception.ValidationException("Неверные параметры бизнес-логики");
    }

    @GetMapping("/test/user-not-found")
    public void throwUserNotFound() {
        throw new UserNotFoundException("Пользователь с id=42 не найден");
    }

    @GetMapping("/test/movie-not-found")
    public void throwMovieNotFound() {
        throw new MoviePresenceInListException("Фильм с id=777 не найден");
    }

    @GetMapping("/test/illegal-argument")
    public void throwIllegalArgument() {
        throw new IllegalArgumentException("Некорректный аргумент метода");
    }

    @GetMapping("/test/runtime")
    public void throwRuntime() {
        throw new RuntimeException("Непредвиденная критическая ошибка");
    }

    @GetMapping("/test/json-parse-mpa")
    public void throwJsonMpa() {
        throw new HttpMessageNotReadableException(
                "JSON parse error: Cannot construct instance of ru.yandex.practicum.filmorate.model.MpaRating, problem: Unknown MpaRating id: 10",
                new IllegalArgumentException("Unknown MpaRating id: 10"),
                null
        );
    }
}
