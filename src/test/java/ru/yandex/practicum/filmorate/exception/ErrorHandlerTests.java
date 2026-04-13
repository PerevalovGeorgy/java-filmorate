package ru.yandex.practicum.filmorate.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTests {

    @Test
    void shouldCreateErrorResponse() {
        ErrorResponse response = new ErrorResponse("Test Error", "Test Description");

        assertEquals("Test Error", response.getError());
        assertEquals("Test Description", response.getDescription());
    }

    @Test
    void shouldCreateValidationException() {
        ValidationException exception = new ValidationException("Validation error");

        assertEquals("Validation error", exception.getMessage());
    }

    @Test
    void shouldCreateUserNotFoundException() {
        UserNotFoundException exception = new UserNotFoundException("User not found");

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldCreateMoviePresenceInListException() {
        MoviePresenceInListException exception = new MoviePresenceInListException("Movie not found");

        assertEquals("Movie not found", exception.getMessage());
    }

    @Test
    void shouldCreateUserPresenceInListException() {
        UserPresenceInListException exception = new UserPresenceInListException("User not in list");

        assertEquals("User not in list", exception.getMessage());
    }
}