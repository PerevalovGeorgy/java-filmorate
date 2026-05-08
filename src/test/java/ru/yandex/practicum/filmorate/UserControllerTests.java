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
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.NewUserDto;
import ru.yandex.practicum.filmorate.dto.UpdateUserDto;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(UserController.class)
@Import({UserMapper.class, ErrorHandler.class})
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .email("user@mail.ru")
                .login("userLogin")
                .name("User Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
    }

    @Test
    void testFindAllUsers() throws Exception {
        Mockito.when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("user@mail.ru"))
                .andExpect(jsonPath("$[0].login").value("userLogin"));
    }

    @Test
    void testFindUserById() throws Exception {
        Mockito.when(userService.findById(1)).thenReturn(user);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User Name"));
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        NewUserDto newUserDto = NewUserDto.builder()
                .email("new@mail.ru")
                .login("newLogin")
                .name("New User")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        Mockito.when(userService.create(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUserValidationFailEmail() throws Exception {
        NewUserDto invalidUserDto = NewUserDto.builder()
                .email("invalid-email-no-at")
                .login("login")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации параметров запроса"));
    }

    @Test
    void testCreateUserValidationFailLoginWithSpaces() throws Exception {
        NewUserDto invalidUserDto = NewUserDto.builder()
                .email("user@mail.ru")
                .login("invalid login spaces")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("login")));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        UpdateUserDto updateUserDto = UpdateUserDto.builder()
                .id(1)
                .email("updated@mail.ru")
                .login("updatedLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Mockito.when(userService.update(any(User.class))).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateUserValidationFailNoId() throws Exception {
        UpdateUserDto invalidUpdateDto = UpdateUserDto.builder()
                .id(null)
                .email("updated@mail.ru")
                .login("updatedLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddFriendOneWay() throws Exception {
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).addFriend(1, 2);
    }

    @Test
    void testRemoveFriend() throws Exception {
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).removeFriend(1, 2);
    }

    @Test
    void testGetFriends() throws Exception {
        Mockito.when(userService.getFriends(1)).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }
}
