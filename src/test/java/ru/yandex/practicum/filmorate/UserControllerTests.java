package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController userController;
    private User validUser;
    private User secondUser;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);

        validUser = User.builder()
                .email("user@example.com")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        secondUser = User.builder()
                .email("second@example.com")
                .login("secondLogin")
                .name("Second Name")
                .birthday(LocalDate.of(1992, 2, 2))
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
    void shouldFindAllUsers() {
        userController.create(validUser);
        userController.create(secondUser);

        Collection<User> allUsers = userController.findAll();

        assertEquals(2, allUsers.size());
    }

    @Test
    void shouldFindUserById() {
        User created = userController.create(validUser);

        User found = userController.findById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getLogin(), found.getLogin());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        assertThrows(UserNotFoundException.class,
                () -> userController.findById(999));
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
        assertEquals("Email не может быть пустым", exception.getMessage());
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
        assertEquals("Email не может быть пустым", exception.getMessage());
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
        assertEquals("Email должен содержать @", exception.getMessage());
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

        UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userController.update(user));
        assertEquals("Пользователь с id = 999 не найден", exception.getMessage());
    }

    @Test
    void shouldAddFriend() {
        User user1 = userController.create(validUser);
        User user2 = userController.create(secondUser);

        userController.addFriend(user1.getId(), user2.getId());

        Collection<User> friends = userController.getFriends(user1.getId());
        assertEquals(1, friends.size());
        assertEquals(user2.getId(), friends.iterator().next().getId());
    }

    @Test
    void shouldAddMutualFriends() {
        User user1 = userController.create(validUser);
        User user2 = userController.create(secondUser);

        userController.addFriend(user1.getId(), user2.getId());

        Collection<User> friends1 = userController.getFriends(user1.getId());
        Collection<User> friends2 = userController.getFriends(user2.getId());

        assertEquals(1, friends1.size());
        assertEquals(1, friends2.size());
    }

    @Test
    void shouldThrowExceptionWhenAddSelfAsFriend() {
        User user = userController.create(validUser);

        assertThrows(ValidationException.class,
                () -> userController.addFriend(user.getId(), user.getId()));
    }

    @Test
    void shouldRemoveFriend() {
        User user1 = userController.create(validUser);
        User user2 = userController.create(secondUser);

        userController.addFriend(user1.getId(), user2.getId());
        userController.removeFriend(user1.getId(), user2.getId());

        Collection<User> friends = userController.getFriends(user1.getId());
        assertTrue(friends.isEmpty());
    }

    @Test
    void shouldGetFriendsList() {
        User user1 = userController.create(validUser);
        User user2 = userController.create(secondUser);

        User user3 = User.builder()
                .email("third@example.com")
                .login("thirdLogin")
                .name("Third Name")
                .birthday(LocalDate.of(1993, 3, 3))
                .build();
        User user3created = userController.create(user3);

        userController.addFriend(user1.getId(), user2.getId());
        userController.addFriend(user1.getId(), user3created.getId());

        Collection<User> friends = userController.getFriends(user1.getId());
        assertEquals(2, friends.size());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = userController.create(validUser);
        User user2 = userController.create(secondUser);

        User commonFriend = User.builder()
                .email("common@example.com")
                .login("commonLogin")
                .name("Common Friend")
                .birthday(LocalDate.of(1994, 4, 4))
                .build();
        User common = userController.create(commonFriend);

        userController.addFriend(user1.getId(), common.getId());
        userController.addFriend(user2.getId(), common.getId());

        Collection<User> commonFriends = userController.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(common.getId(), commonFriends.iterator().next().getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoCommonFriends() {
        User user1 = userController.create(validUser);
        User user2 = userController.create(secondUser);

        Collection<User> commonFriends = userController.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenAddFriendToNonExistentUser() {
        User user = userController.create(validUser);

        assertThrows(UserNotFoundException.class,
                () -> userController.addFriend(user.getId(), 999));
    }

    @Test
    void shouldCreateUserWithMinimalValidData() {
        User user = User.builder()
                .email("a@b.com")
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