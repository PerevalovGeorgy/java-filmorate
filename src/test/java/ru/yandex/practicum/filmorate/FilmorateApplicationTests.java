package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        UserDbStorage.class, UserRepository.class, UserRowMapper.class,
        FilmDbStorage.class, FilmRepository.class, FilmRowMapper.class,
        GenreRepository.class, GenreRowMapper.class,
        MpaRepository.class, MpaRowMapper.class
})
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;

    private User user1;
    private User user2;
    private Film film1;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .email("user1@mail.ru")
                .login("user1Login")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        user2 = User.builder()
                .email("user2@mail.ru")
                .login("user2Login")
                .name("User Two")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();

        film1 = Film.builder()
                .name("Inception")
                .description("A thief who steals corporate secrets...")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .mpa(MpaRating.PG_13)
                .genres(new LinkedHashSet<>())
                .build();
    }


    @Test
    void testCreateAndFindUserById() {
        User createdUser = userStorage.create(user1);
        Optional<User> userOptional = userStorage.findById(createdUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user).hasFieldOrPropertyWithValue("id", createdUser.getId());
                    assertThat(user).hasFieldOrPropertyWithValue("email", "user1@mail.ru");
                    assertThat(user).hasFieldOrPropertyWithValue("login", "user1Login");
                });
    }

    @Test
    void testFindAllUsers() {
        userStorage.create(user1);
        userStorage.create(user2);
        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
    }

    @Test
    void testUpdateUser() {
        User createdUser = userStorage.create(user1);
        createdUser.setName("Updated Name");

        userStorage.update(createdUser);
        Optional<User> updatedUserOpt = userStorage.findById(createdUser.getId());

        assertThat(updatedUserOpt)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("name", "Updated Name")
                );
    }

    @Test
    void testExistsById() {
        User createdUser = userStorage.create(user1);
        assertThat(userStorage.existsById(createdUser.getId())).isTrue();
        assertThat(userStorage.existsById(999)).isFalse();
    }

    @Test
    void testDeleteUser() {
        User createdUser = userStorage.create(user1);
        userStorage.delete(createdUser.getId());
        assertThat(userStorage.findById(createdUser.getId())).isEmpty();
    }

    @Test
    void testFriendsLogicOneWay() {
        User u1 = userStorage.create(user1);
        User u2 = userStorage.create(user2);

        userStorage.addFriend(u1.getId(), u2.getId());

        Collection<User> u1Friends = userStorage.getFriends(u1.getId());
        assertThat(u1Friends).hasSize(1).extracting(User::getId).contains(u2.getId());

        Collection<User> u2Friends = userStorage.getFriends(u2.getId());
        assertThat(u2Friends).isEmpty();

        userStorage.removeFriend(u1.getId(), u2.getId());
        assertThat(userStorage.getFriends(u1.getId())).isEmpty();
    }


    @Test
    void testCreateAndFindFilmById() {
        film1.setMpa(MpaRating.G);
        Film createdFilm = filmStorage.create(film1);
        Optional<Film> filmOptional = filmStorage.findById(createdFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", createdFilm.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Inception");
                    assertThat(film.getMpa()).isEqualTo(MpaRating.G);
                });
    }

    @Test
    void testUpdateFilmAndGenres() {
        film1.setMpa(MpaRating.G);
        Film createdFilm = filmStorage.create(film1);

        createdFilm.setName("Inception Updated");
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(Genre.COMEDY);
        createdFilm.setGenres(genres);

        filmStorage.update(createdFilm);
        Optional<Film> updatedFilmOpt = filmStorage.findById(createdFilm.getId());

        assertThat(updatedFilmOpt)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Inception Updated");
                    assertThat(film.getGenres()).hasSize(1).contains(Genre.COMEDY);
                });
    }

    @Test
    void testLikesAndPopularFilms() {
        film1.setMpa(MpaRating.G);
        Film f1 = filmStorage.create(film1);
        User u1 = userStorage.create(user1);

        filmStorage.addLike(f1.getId(), u1.getId());
        Collection<Film> popular = filmStorage.getPopularFilms(1);

        assertThat(popular).hasSize(1).extracting(Film::getId).contains(f1.getId());
        filmStorage.removeLike(f1.getId(), u1.getId());
    }

    @Test
    void testFindAllGenresFromDb() {
        Collection<Genre> genres = genreRepository.findAll();

        assertThat(genres)
                .hasSize(6)
                .contains(Genre.COMEDY, Genre.DRAMA, Genre.ACTION);
    }

    @Test
    void testFindGenreByIdFromDb() {
        Optional<Genre> comedyOpt = genreRepository.findById(1);
        assertThat(comedyOpt).isPresent().hasValue(Genre.COMEDY);

        Optional<Genre> unknownGenreOpt = genreRepository.findById(999);
        assertThat(unknownGenreOpt).isEmpty();
    }

    @Test
    void testFindAllMpaFromDb() {
        Collection<MpaRating> mpaList = mpaRepository.findAll();

        assertThat(mpaList)
                .hasSize(5)
                .contains(MpaRating.G, MpaRating.R, MpaRating.NC_17);
    }

    @Test
    void testFindMpaByIdFromDb() {
        Optional<MpaRating> gRatingOpt = mpaRepository.findById(1);
        assertThat(gRatingOpt).isPresent().hasValue(MpaRating.G);

        Optional<MpaRating> unknownMpaOpt = mpaRepository.findById(999);
        assertThat(unknownMpaOpt).isEmpty();
    }
}
