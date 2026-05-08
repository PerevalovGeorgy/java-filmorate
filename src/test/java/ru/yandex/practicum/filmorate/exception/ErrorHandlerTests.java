package ru.yandex.practicum.filmorate.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExceptionTestController.class)
@Import(ErrorHandler.class)
class ErrorHandlerTests {

    @Autowired
    private MockMvc mockMvc;


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

    @Test
    void handleValidationException_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/test/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"))
                .andExpect(jsonPath("$.description").value("Неверные параметры бизнес-логики"));
    }

    @Test
    void handleUserNotFoundException_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/test/user-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"))
                .andExpect(jsonPath("$.description").value("Пользователь с id=42 не найден"));
    }

    @Test
    void handleMoviePresenceInListException_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/test/movie-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм не найден"))
                .andExpect(jsonPath("$.description").value("Фильм с id=777 не найден"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Некорректный аргумент запроса"))
                .andExpect(jsonPath("$.description").value("Некорректный аргумент метода"));
    }

    @Test
    void handleOtherExceptions_ShouldReturn500() throws Exception {
        mockMvc.perform(get("/test/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Внутренняя ошибка сервера"))
                .andExpect(jsonPath("$.description").value("Непредвиденная критическая ошибка"));
    }

    @Test
    void handleHttpMessageNotReadableException_WithMpaRating_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/test/json-parse-mpa"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.description").value("Unknown MpaRating id: 10"));
    }
}
