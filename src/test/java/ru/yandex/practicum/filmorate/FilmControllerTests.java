package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
@Import({FilmMapper.class, ErrorHandler.class})
public class FilmControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private Film film;

    @BeforeEach
    void setUp() {
        film = Film.builder()
                .id(1)
                .name("Начало")
                .description("Культовый научно-фантастический триллер")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(MpaRating.PG_13)
                .genres(new LinkedHashSet<>())
                .build();
    }

    @Test
    void testFindAllFilms() throws Exception {
        Mockito.when(filmService.findAll()).thenReturn(List.of(film));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Начало"))
                .andExpect(jsonPath("$[0].duration").value(148));
    }

    @Test
    void testFindFilmById() throws Exception {
        Mockito.when(filmService.findById(1)).thenReturn(film);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Начало"));
    }

    @Test
    void testCreateFilmSuccess() throws Exception {
        NewFilmDto newFilmDto = NewFilmDto.builder()
                .name("Интерстеллар")
                .description("Фильм про космос")
                .releaseDate(LocalDate.of(2014, 11, 6))
                .duration(169)
                .mpa(MpaRating.PG_13)
                .build();

        Mockito.when(filmService.create(any(Film.class))).thenReturn(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilmDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateFilmValidationFailBlankName() throws Exception {
        NewFilmDto invalidDto = NewFilmDto.builder()
                .name("   ")
                .description("Описание")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(MpaRating.G)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации параметров запроса"))
                .andExpect(jsonPath("$.description", containsString("name")));
    }

    @Test
    void testCreateFilmValidationFailLongDescription() throws Exception {
        String longDescription = "a".repeat(201);

        NewFilmDto invalidDto = NewFilmDto.builder()
                .name("Фильм")
                .description(longDescription)
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120)
                .mpa(MpaRating.G)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("description")));
    }

    @Test
    void testCreateFilmValidationFailNegativeDuration() throws Exception {
        NewFilmDto invalidDto = NewFilmDto.builder()
                .name("Фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(-10)
                .mpa(MpaRating.G)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("duration")));
    }

    @Test
    void testUpdateFilmSuccess() throws Exception {
        UpdateFilmDto updateFilmDto = UpdateFilmDto.builder()
                .id(1)
                .name("Начало Изменено")
                .description("Новое описание")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(MpaRating.PG_13)
                .build();

        Mockito.when(filmService.update(any(Film.class))).thenReturn(film);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFilmDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateFilmValidationFailNoId() throws Exception {
        UpdateFilmDto invalidDto = UpdateFilmDto.builder()
                .id(null)
                .name("Название")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(MpaRating.PG_13)
                .build();

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("id")));
    }

    @Test
    void testSetLike() throws Exception {
        mockMvc.perform(put("/films/1/like/2"))
                .andExpect(status().isOk());

        Mockito.verify(filmService, Mockito.times(1)).setLikeFilm(1, 2);
    }

    @Test
    void testDeleteLike() throws Exception {
        mockMvc.perform(delete("/films/1/like/2"))
                .andExpect(status().isOk());

        Mockito.verify(filmService, Mockito.times(1)).deleteLikeFilm(1, 2);
    }

    @Test
    void testGetPopularFilms() throws Exception {
        Mockito.when(filmService.getFilmsByLikes(5)).thenReturn(Collections.singletonList(film));

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
