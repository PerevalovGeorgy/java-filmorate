package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.dto.NewFilmDto;
import ru.yandex.practicum.filmorate.dto.UpdateFilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private FilmDto validFilmDto;
    private MpaRatingDto validMpaDto;

    @BeforeEach
    void setUp() {
        validMpaDto = MpaRatingDto.builder().id(1).name("G").build();
        validFilmDto = FilmDto.builder()
                .id(1)
                .name("Interstellar")
                .description("A team of explorers travel through a wormhole in space.")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(169)
                .mpa(validMpaDto)
                .genres(new LinkedHashSet<>())
                .build();
    }

    @Test
    void findAll_ShouldReturnOkAndFilmsList() throws Exception {
        Mockito.when(filmService.findAll()).thenReturn(List.of(validFilmDto));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Interstellar"));
    }

    @Test
    void findById_ShouldReturnOkAndFilm_WhenFilmExists() throws Exception {
        Mockito.when(filmService.findById(1)).thenReturn(validFilmDto);

        mockMvc.perform(get("/films/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Interstellar"));
    }

    @Test
    void create_ShouldReturnCreatedAndFilmDto_WhenDataIsValid() throws Exception {
        NewFilmDto newFilmDto = NewFilmDto.builder()
                .name("Inception")
                .description("A thief who steals corporate secrets through the use of dream-sharing.")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(validMpaDto)
                .genres(new LinkedHashSet<>())
                .build();

        Mockito.when(filmService.create(any(NewFilmDto.class))).thenReturn(validFilmDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newFilmDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenNameIsBlank() throws Exception {
        NewFilmDto invalidDto = NewFilmDto.builder()
                .name(" ")
                .description("Valid description")
                .releaseDate(LocalDate.now())
                .duration(120)
                .mpa(validMpaDto)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_ShouldReturnBadRequest_WhenDescriptionIsTooLong() throws Exception {
        String longDescription = "a".repeat(201);
        NewFilmDto invalidDto = NewFilmDto.builder()
                .name("Valid Name")
                .description(longDescription)
                .releaseDate(LocalDate.now())
                .duration(120)
                .mpa(validMpaDto)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_ShouldReturnOkAndUpdatedFilm_WhenDataIsValid() throws Exception {
        UpdateFilmDto updateDto = UpdateFilmDto.builder()
                .id(1)
                .name("Interstellar Updated")
                .description("Updated description")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .duration(170)
                .mpa(validMpaDto)
                .build();

        Mockito.when(filmService.update(any(UpdateFilmDto.class))).thenReturn(validFilmDto);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    void setLike_ShouldReturnOk() throws Exception {
        Mockito.doNothing().when(filmService).setLikeFilm(1, 2);

        mockMvc.perform(put("/films/1/like/2"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteLike_ShouldReturnOk() throws Exception {
        Mockito.doNothing().when(filmService).deleteLikeFilm(1, 2);

        mockMvc.perform(delete("/films/1/like/2"))
                .andExpect(status().isOk());
    }

    @Test
    void getPopularFilms_ShouldReturnOkAndListWithDefaultCount() throws Exception {
        Mockito.when(filmService.getFilmsByLikes(10)).thenReturn(List.of(validFilmDto));

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getPopularFilms_ShouldReturnOkAndListWithCustomCount() throws Exception {
        Mockito.when(filmService.getFilmsByLikes(5)).thenReturn(List.of(validFilmDto));

        mockMvc.perform(get("/films/popular").param("count", "5"))
                .andExpect(status().isOk());
    }
}
