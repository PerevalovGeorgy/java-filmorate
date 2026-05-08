package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
@Import(ErrorHandler.class)
public class GenreControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    @Test
    void testFindAllGenres() throws Exception {
        Mockito.when(genreService.findAll()).thenReturn(List.of(Genre.COMEDY, Genre.DRAMA));

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Комедия"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Драма"));
    }

    @Test
    void testFindGenreByIdSuccess() throws Exception {
        Mockito.when(genreService.findById(1)).thenReturn(Genre.COMEDY);

        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));
    }

    @Test
    void testFindGenreByIdNotFound() throws Exception {
        Mockito.when(genreService.findById(999))
                .thenThrow(new MoviePresenceInListException("Жанр с id=999 не найден"));

        mockMvc.perform(get("/genres/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм не найден"))
                .andExpect(jsonPath("$.description").value("Жанр с id=999 не найден"));
    }
}
