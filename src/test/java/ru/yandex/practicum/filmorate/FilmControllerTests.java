package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTests {

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = Film.builder()
                .name("Valid Film")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120) // продолжительность в минутах
                .build();
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

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(film));
        assertEquals("Фильм с id = 999 не найден", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithInvalidName() {
        Film created = filmController.create(validFilm);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("")
                .description("Updated description")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(150)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updatedFilm));
        assertEquals("Название не может быть пустым", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithTooLongDescription() {
        Film created = filmController.create(validFilm);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("Updated Film")
                .description("A".repeat(201))
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(150)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updatedFilm));
        assertEquals("Максимальная длина описания — 200 символов", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenUpdateWithInvalidDuration() {
        Film created = filmController.create(validFilm);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("Updated Film")
                .description("Updated description")
                .releaseDate(LocalDate.of(2010, 1, 1))
                .duration(0)
                .build();

        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.update(updatedFilm));
        assertEquals("Продолжительность должна быть положительной", exception.getMessage());
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