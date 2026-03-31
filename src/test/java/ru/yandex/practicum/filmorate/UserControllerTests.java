package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void shouldCreateUserWithValidData() {
        User created = userController.create(validUser);

        assertNotNull(created.getId());
        assertEquals("user@example.com", created.getEmail());
        assertEquals("validLogin", created.getLogin());
        assertEquals("Valid Name", created.getName());
        assertEquals(LocalDate.of(1990, 1, 1), created.getBirthday());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        User user = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name(null)
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userController.create(user);
        assertEquals("validLogin", created.getName());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsEmpty() {
        User user = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userController.create(user);
        assertEquals("validLogin", created.getName());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsBlank() {
        User user = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("   ")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userController.create(user);
        assertEquals("validLogin", created.getName());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        User user = User.builder()
                .email(null)
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Email должен содержать @ и не быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        User user = User.builder()
                .email("")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Email должен содержать @ и не быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAtSymbol() {
        User user = User.builder()
                .email("user.example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Email должен содержать @ и не быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenEmailHasInvalidFormat() {
        User user = User.builder()
                .email("это-неправильный?эмейл@.")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Email должен быть корректным (например: user@example.com)", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        User user = User.builder()
                .email("user@example.com")
                .login(null)
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLoginIsEmpty() {
        User user = User.builder()
                .email("user@example.com")
                .login("")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Логин не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        User user = User.builder()
                .email("user@example.com")
                .login("invalid login")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldCreateUserWithNullBirthday() {
        User user = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(null)
                .build();

        User created = userController.create(user);
        assertNull(created.getBirthday());
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        User user = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.create(user));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void shouldCreateUserWithBirthdayToday() {
        User user = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.now())
                .build();

        User created = userController.create(user);
        assertEquals(LocalDate.now(), created.getBirthday());
    }

    @Test
    void shouldUpdateExistingUser() {
        User created = userController.create(validUser);

        User updatedUser = User.builder()
                .id(created.getId())
                .email("updated@example.com")
                .login("updatedLogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        User updated = userController.update(updatedUser);
        assertEquals("updated@example.com", updated.getEmail());
        assertEquals("updatedLogin", updated.getLogin());
        assertEquals("Updated Name", updated.getName());
        assertEquals(LocalDate.of(1995, 5, 5), updated.getBirthday());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithNullId() {
        User user = User.builder()
                .id(null)
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(user));
        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistentUser() {
        User user = User.builder()
                .id(999)
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(user));
        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithInvalidEmail() {
        User created = userController.create(validUser);

        User updatedUser = User.builder()
                .id(created.getId())
                .email("invalid-email")
                .login("updatedLogin")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updatedUser));
        assertEquals("Email должен содержать @ и не быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithInvalidLogin() {
        User created = userController.create(validUser);

        User updatedUser = User.builder()
                .id(created.getId())
                .email("updated@example.com")
                .login("invalid login")
                .name("Updated Name")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updatedUser));
        assertEquals("Логин не может содержать пробелы", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithBirthdayInFuture() {
        User created = userController.create(validUser);

        User updatedUser = User.builder()
                .id(created.getId())
                .email("updated@example.com")
                .login("updatedLogin")
                .name("Updated Name")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.update(updatedUser));
        assertEquals("Дата рождения не может быть в будущем", exception.getMessage());
    }

    @Test
    void shouldCreateUserWithMinimalValidData() {
        User user = User.builder()
                .email("a@b.com")  // используем .com вместо .c
                .login("a")
                .name(null)
                .birthday(null)
                .build();

        User created = userController.create(user);
        assertEquals("a", created.getLogin());
        assertEquals("a", created.getName());
    }

    @Test
    void shouldCreateUserWithMaxLengthData() {
        String longLogin = "a".repeat(255);
        String longEmail = longLogin + "@example.com";

        User user = User.builder()
                .email(longEmail)
                .login(longLogin)
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userController.create(user);
        assertEquals(longLogin, created.getLogin());
        assertEquals(longEmail, created.getEmail());
    }
}