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
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.ErrorHandler;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserMapper.class, ErrorHandler.class})
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private FeedService feedService;

    private UserDto validUserDto;
    private Feed validFeed;

    @BeforeEach
    void setUp() {
        validUserDto = UserDto.builder()
                .id(1)
                .email("user@mail.ru")
                .login("userLogin")
                .name("User Name")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        validFeed = Feed.builder()
                .eventId(1)
                .timestamp(System.currentTimeMillis())
                .userId(1)
                .eventType("FRIEND")
                .operation("ADD")
                .entityId(2)
                .build();
    }

    @Test
    void testFindAllUsers() throws Exception {
        Mockito.when(userService.findAll()).thenReturn(List.of(validUserDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("user@mail.ru"))
                .andExpect(jsonPath("$[0].login").value("userLogin"));
    }

    @Test
    void testFindUserById() throws Exception {
        Mockito.when(userService.findById(1)).thenReturn(validUserDto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User Name"));
    }

    @Test
    void testCreateUserSuccess() throws Exception {
        UserDto newUserDto = UserDto.builder()
                .email("new@mail.ru")
                .login("newLogin")
                .name("New User")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        Mockito.when(userService.create(any(UserDto.class))).thenReturn(validUserDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testCreateUserValidationFailEmail() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .email("invalid-email-no-at")
                .login("login")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка валидации"));
    }

    @Test
    void testCreateUserValidationFailLoginWithSpaces() throws Exception {
        UserDto invalidUserDto = UserDto.builder()
                .email("user@mail.ru")
                .login("invalid login spaces")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description", containsString("Логин не может содержать пробелы")));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        UserDto updateUserDto = UserDto.builder()
                .id(1)
                .email("updated@mail.ru")
                .login("updatedLogin")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Mockito.when(userService.update(any(UserDto.class))).thenReturn(validUserDto);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testUpdateUserValidationFailFutureBirthday() throws Exception {
        UserDto invalidUpdateDto = UserDto.builder()
                .id(1)
                .email("updated@mail.ru")
                .login("updatedLogin")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteUser() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(1);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).deleteUser(1);
    }

    @Test
    void testAddFriend() throws Exception {
        Mockito.doNothing().when(userService).addFriend(1, 2);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).addFriend(1, 2);
    }

    @Test
    void testRemoveFriend() throws Exception {
        Mockito.doNothing().when(userService).removeFriend(1, 2);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk());

        Mockito.verify(userService, Mockito.times(1)).removeFriend(1, 2);
    }

    @Test
    void testGetFriends() throws Exception {
        Mockito.when(userService.getFriends(1)).thenReturn(Collections.singletonList(validUserDto));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetCommonFriends() throws Exception {
        Mockito.when(userService.getCommonFriends(1, 2)).thenReturn(List.of(validUserDto));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void testGetFeed() throws Exception {
        Mockito.when(feedService.getFeed(1)).thenReturn(List.of(validFeed));

        mockMvc.perform(get("/users/1/feed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].eventId").value(1))
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].eventType").value("FRIEND"))
                .andExpect(jsonPath("$[0].operation").value("ADD"));
    }

    @Test
    void testGetFeedUserNotFound() throws Exception {
        Mockito.when(feedService.getFeed(999)).thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Пользователь с id 999 не найден"));

        mockMvc.perform(get("/users/999/feed"))
                .andExpect(status().isNotFound());
    }
}