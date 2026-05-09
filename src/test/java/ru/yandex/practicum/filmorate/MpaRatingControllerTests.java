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
import ru.yandex.practicum.filmorate.controller.MpaRatingController;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MpaRatingController.class)
@Import(ErrorHandler.class)
public class MpaRatingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MpaRatingService mpaRatingService;

    private MpaRatingDto mpaG;
    private MpaRatingDto mpaPg;
    private MpaRatingDto mpaPg13;

    @BeforeEach
    void setUp() {
        mpaG = MpaRatingDto.builder().id(1).name("G").build();
        mpaPg = MpaRatingDto.builder().id(2).name("PG").build();
        mpaPg13 = MpaRatingDto.builder().id(3).name("PG-13").build();
    }

    @Test
    void testFindAllMpa() throws Exception {
        Mockito.when(mpaRatingService.findAll()).thenReturn(List.of(mpaG, mpaPg, mpaPg13));

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("PG-13"));
    }

    @Test
    void testFindMpaByIdSuccess() throws Exception {
        Mockito.when(mpaRatingService.findById(3)).thenReturn(mpaPg13);

        mockMvc.perform(get("/mpa/3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("PG-13"));
    }

    @Test
    void testFindMpaByIdNotFound() throws Exception {
        Mockito.when(mpaRatingService.findById(999))
                .thenThrow(new NotFoundException("Рейтинг MPA с id 999 не найден"));

        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ресурс не найден"))
                .andExpect(jsonPath("$.description").value("Рейтинг MPA с id 999 не найден"));
    }
}
