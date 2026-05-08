package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.MpaController;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MpaController.class)
@Import(ErrorHandler.class)
public class MpaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MpaService mpaService;

    @Test
    void testFindAllMpa() throws Exception {
        Mockito.when(mpaService.findAll()).thenReturn(List.of(MpaRating.G, MpaRating.PG, MpaRating.PG_13));

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].name").value("PG-13"));
    }


    @Test
    void testFindMpaByIdSuccess() throws Exception {
        Mockito.when(mpaService.findById(3)).thenReturn(MpaRating.PG_13);

        mockMvc.perform(get("/mpa/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("PG-13"));
    }

    @Test
    void testFindMpaByIdNotFound() throws Exception {
        Mockito.when(mpaService.findById(999))
                .thenThrow(new MoviePresenceInListException("Рейтинг MPA с id=999 не найден"));

        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм не найден"))
                .andExpect(jsonPath("$.description").value("Рейтинг MPA с id=999 не найден"));
    }
}
