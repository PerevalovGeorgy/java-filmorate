package ru.yandex.practicum.filmorate.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException e) {
        log.error("Ресурс не найден: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Ресурс не найден", e.getMessage()));
    }

    @ExceptionHandler(ru.yandex.practicum.filmorate.exception.ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(ru.yandex.practicum.filmorate.exception.ValidationException e) {
        log.error("Ошибка валидации бизнес-логики: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Ошибка валидации", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("Ошибка валидации DTO: {}", errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Ошибка валидации параметров запроса", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String message = e.getMessage();
        if (message != null && (message.contains("MpaRating") || message.contains("Genre"))) {
            log.error("Ресурс справочника не найден при десериализации: {}", message);
            String description = e.getCause() != null ? e.getCause().getMessage() : message;
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Ресурс не найден", description));
        }
        log.error("Ошибка чтения JSON-тела запроса: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Ошибка валидации параметров запроса", message));
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        log.error("Пользователь не найден: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Пользователь не найден", e.getMessage()));
    }

    @ExceptionHandler(MoviePresenceInListException.class)
    public ResponseEntity<ErrorResponse> handleMoviePresenceInListException(MoviePresenceInListException e) {
        log.error("Фильм не найден: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Фильм не найден", e.getMessage()));
    }

    @ExceptionHandler(UserPresenceInListException.class)
    public ResponseEntity<ErrorResponse> handleUserPresenceInListException(UserPresenceInListException e) {
        log.error("Пользователь не найден в списке: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Пользователь не найден", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Некорректный аргумент: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Некорректный аргумент запроса", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherExceptions(Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Внутренняя ошибка сервера", e.getMessage()));
    }
}
