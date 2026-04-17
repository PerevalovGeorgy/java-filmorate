package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.MoviePresenceInListException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {

    private FilmController filmController;
    private UserController userController;
    private Film validFilm;
    private User testUser;

    @BeforeEach
    void setUp() {
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        FilmService filmService = new FilmService(filmStorage, userService);

        filmController = new FilmController(filmService);
        userController = new UserController(userService);

        validFilm = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        // Создаем тестового пользователя для тестов лайков
        testUser = User.builder()
                .email("test@example.com")
                .login("testUser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userController.create(testUser);
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film created = filmController.create(validFilm);

        assertNotNull(created.getId());
        assertEquals("Valid Film", created.getName());
        assertEquals("Valid description", created.getDescription());
        assertEquals(LocalDate.of(2000, 1, 1), created.getReleaseDate());
        assertEquals(120, created.getDuration());
    }

    @Test
    void shouldFindAllFilms() {
        filmController.create(validFilm);
        Film secondFilm = Film.builder()
                .name("Second Film")
                .description("Second description")
                .releaseDate(LocalDate.of(2001, 1, 1))
                .duration(90)
                .build();
        filmController.create(secondFilm);

        Collection<Film> allFilms = filmController.findAll();

        assertEquals(2, allFilms.size());
    }

    @Test
    void shouldFindFilmById() {
        Film created = filmController.create(validFilm);

        Film found = filmController.findById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    void shouldThrowExceptionWhenFilmNotFound() {
        assertThrows(MoviePresenceInListException.class,
                () -> filmController.findById(999));
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        Film film = Film.builder()
                .name(null)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        Film film = Film.builder()
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        Film film = Film.builder()
                .name("   ")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldCreateFilmWithNullDescription() {
        Film film = Film.builder()
                .name("Valid Film")
                .description(null)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film created = filmController.create(film);
        assertNull(created.getDescription());
    }

    @Test
    void shouldCreateFilmWithDescriptionExactly200Chars() {
        String description200 = "A".repeat(200);
        Film film = Film.builder()
                .name("Valid Film")
                .description(description200)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film created = filmController.create(film);
        assertEquals(200, created.getDescription().length());
    }

    @Test
    void shouldThrowExceptionWhenDescriptionExceeds200Chars() {
        String description201 = "A".repeat(201);
        Film film = Film.builder()
                .name("Valid Film")
                .description(description201)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void shouldCreateFilmWithNullReleaseDate() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(null)
                .duration(120)
                .build();

        Film created = filmController.create(film);
        assertNull(created.getReleaseDate());
    }

    @Test
    void shouldCreateFilmWithReleaseDateOnBorder() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(120)
                .build();

        Film created = filmController.create(film);
        assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsTooEarly() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года", exception.getMessage());
    }

    @Test
    void shouldCreateFilmWithReleaseDateInFuture() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.now().plusYears(1))
                .duration(120)
                .build();

        Film created = filmController.create(film);
        assertEquals(LocalDate.now().plusYears(1), created.getReleaseDate());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Продолжительность должна быть положительной", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.create(film));
        assertEquals("Продолжительность должна быть положительной", exception.getMessage());
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film created = filmController.create(validFilm);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(150)
                .build();

        Film updated = filmController.update(updatedFilm);
        assertEquals("Updated Film", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(LocalDate.of(2010, 1, 1), updated.getReleaseDate());
        assertEquals(150, updated.getDuration());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithNullId() {
        Film film = Film.builder()
                .id(null)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(film));
        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistentFilm() {
        Film film = Film.builder()
                .id(999)
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        MoviePresenceInListException exception = assertThrows(MoviePresenceInListException.class,
                () -> filmController.update(film));
        assertEquals("Фильм с id = 999 не найден", exception.getMessage());
    }

    @Test
    void shouldSetLikeOnFilm() {
        Film created = filmController.create(validFilm);

        filmController.setLike(created.getId(), testUser.getId());

        Film filmWithLike = filmController.findById(created.getId());
        assertTrue(filmWithLike.likes.contains(testUser.getId()));
    }

    @Test
    void shouldRemoveLikeFromFilm() {
        Film created = filmController.create(validFilm);
        filmController.setLike(created.getId(), testUser.getId());

        filmController.deleteLike(created.getId(), testUser.getId());

        Film filmWithoutLike = filmController.findById(created.getId());
        assertFalse(filmWithoutLike.likes.contains(testUser.getId()));
    }

    @Test
    void shouldGetPopularFilms() {
        Film film1 = filmController.create(Film.builder()
                .name("Film 1")
                .description("Desc 1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build());

        Film film2 = filmController.create(Film.builder()
                .name("Film 2")
                .description("Desc 2")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build());

        User user2 = User.builder()
                .email("user2@example.com")
                .login("user2")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        userController.create(user2);

        filmController.setLike(film1.getId(), testUser.getId());
        filmController.setLike(film1.getId(), user2.getId());
        filmController.setLike(film2.getId(), testUser.getId());

        Collection<Film> popularFilms = filmController.getPopularFilms(null);

        assertEquals(2, popularFilms.size());
        assertEquals(film1.getId(), popularFilms.iterator().next().getId());
    }

    @Test
    void shouldGetPopularFilmsWithLimit() {
        for (int i = 1; i <= 5; i++) {
            Film film = Film.builder()
                    .name("Film " + i)
                    .description("Desc " + i)
                    .releaseDate(LocalDate.of(2000, 1, 1))
                    .duration(120)
                    .build();
            filmController.create(film);
        }

        Collection<Film> popularFilms = filmController.getPopularFilms(3);

        assertEquals(3, popularFilms.size());
    }

    @Test
    void shouldReturnAllFilmsWhenLimitIsZero() {
        for (int i = 1; i <= 5; i++) {
            Film film = Film.builder()
                    .name("Film " + i)
                    .description("Desc " + i)
                    .releaseDate(LocalDate.of(2000, 1, 1))
                    .duration(120)
                    .build();
            filmController.create(film);
        }

        Collection<Film> popularFilms = filmController.getPopularFilms(0);

        assertEquals(5, popularFilms.size());
    }

    @Test
    void shouldCreateFilmWithMinimalValidData() {
        Film film = Film.builder()
                .name("F")
                .description(null)
                .releaseDate(null)
                .duration(1)
                .build();

        Film created = filmController.create(film);
        assertEquals("F", created.getName());
        assertNull(created.getDescription());
        assertNull(created.getReleaseDate());
        assertEquals(1, created.getDuration());
    }

    @Test
    void shouldCreateFilmWithMaxLengthName() {
        String longName = "A".repeat(255);
        Film film = Film.builder()
                .name(longName)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        Film created = filmController.create(film);
        assertEquals(longName, created.getName());
    }

    @Test
    void shouldCreateFilmWithVeryLongDuration() {
        Film film = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100000)
                .build();

        Film created = filmController.create(film);
        assertEquals(100000, created.getDuration());
    }
}