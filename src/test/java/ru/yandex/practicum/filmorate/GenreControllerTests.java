package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.GenreController;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenreController.class)
@Import(ErrorHandler.class)
public class GenreControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    private GenreDto comedyDto;
    private GenreDto dramaDto;

    @BeforeEach
    void setUp() {
        comedyDto = GenreDto.builder().id(1).name("Комедия").build();
        dramaDto = GenreDto.builder().id(2).name("Драма").build();
    }

    @Test
    void testFindAllGenres() throws Exception {
        Mockito.when(genreService.findAll()).thenReturn(List.of(comedyDto, dramaDto));

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Комедия"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Драма"));
    }

    @Test
    void testFindGenreByIdSuccess() throws Exception {
        Mockito.when(genreService.findById(1)).thenReturn(comedyDto);

        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));
    }

    @Test
    void testFindGenreByIdNotFound() throws Exception {
        Mockito.when(genreService.findById(999))
                .thenThrow(new NotFoundException("Жанр с id 999 не найден"));

        mockMvc.perform(get("/genres/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.description").value("Жанр с id 999 не найден"));
    }
}
