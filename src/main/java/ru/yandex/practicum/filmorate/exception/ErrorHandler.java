package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(ValidationException e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return new ErrorResponse("Ошибка валидации", e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
        log.error("Пользователь не найден: {}", e.getMessage());
        return new ErrorResponse("Пользователь не найден", e.getMessage());
    }

    @ExceptionHandler(MoviePresenceInListException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleMoviePresenceInListException(MoviePresenceInListException e) {
        log.error("Фильм не найден: {}", e.getMessage());
        return new ErrorResponse("Фильм не найден", e.getMessage());
    }

    @ExceptionHandler(UserPresenceInListException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserPresenceInListException(UserPresenceInListException e) {
        log.error("Пользователь не найден: {}", e.getMessage());
        return new ErrorResponse("Пользователь не найден", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleOtherExceptions(Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return new ErrorResponse("Внутренняя ошибка сервера", e.getMessage());
    }
}