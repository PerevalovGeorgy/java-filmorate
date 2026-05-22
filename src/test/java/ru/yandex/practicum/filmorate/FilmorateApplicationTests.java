package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import({
        UserRepository.class,
        UserRowMapper.class,
        FilmRepository.class,
        FilmRowMapper.class,
        GenreRepository.class,
        GenreRowMapper.class,
        MpaRepository.class,
        MpaRatingRowMapper.class
})
class FilmorateApplicationTests {

    private final UserRepository userStorage;
    private final FilmRepository filmStorage;
    private final GenreRepository genreRepository;
    private final MpaRepository mpaRepository;

    private User user1;
    private User user2;
    private Film film1;
    private MpaRating mpaG;
    private Genre genreComedy;

    @BeforeEach
    void setUp() {
        mpaG = MpaRating.builder().id(1).name("G").build();
        genreComedy = Genre.builder().id(1).name("Комедия").build();

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
                .mpa(mpaG)
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
                .hasValueSatisfying(user -> assertThat(user).hasFieldOrPropertyWithValue("name", "Updated Name"));
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
        userStorage.deleteById(createdUser.getId());

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
        Film createdFilm = filmStorage.create(film1);
        Optional<Film> filmOptional = filmStorage.findById(createdFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", createdFilm.getId());
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Inception");
                    assertThat(film.getMpa().getId()).isEqualTo(1);
                    assertThat(film.getMpa().getName()).isEqualTo("G");
                });
    }

    @Test
    void testUpdateFilmAndGenres() {
        Film createdFilm = filmStorage.create(film1);
        createdFilm.setName("Inception Updated");

        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        genres.add(genreComedy);
        createdFilm.setGenres(genres);

        filmStorage.update(createdFilm);

        Optional<Film> updatedFilmOpt = filmStorage.findById(createdFilm.getId());
        assertThat(updatedFilmOpt)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Inception Updated");
                    assertThat(film.getGenres()).hasSize(1).extracting(Genre::getId).contains(1);
                });
    }

    @Test
    void testLikesAndPopularFilms() {
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
                .isNotEmpty()
                .extracting(Genre::getId)
                .contains(1, 2, 3);
    }

    @Test
    void testFindGenreByIdFromDb() {
        Optional<Genre> comedyOpt = genreRepository.findById(1);
        assertThat(comedyOpt).isPresent();
        assertThat(comedyOpt.get().getId()).isEqualTo(1);

        Optional<Genre> unknownGenreOpt = genreRepository.findById(999);
        assertThat(unknownGenreOpt).isEmpty();
    }

    @Test
    void testFindAllMpaFromDb() {
        Collection<MpaRating> mpaList = mpaRepository.findAll();
        assertThat(mpaList)
                .isNotEmpty()
                .extracting(MpaRating.class::cast)
                .extracting(MpaRating::getId)
                .contains(1, 2, 4);
    }

    @Test
    void testFindMpaByIdFromDb() {
        Optional<MpaRating> gRatingOpt = mpaRepository.findById(1);
        assertThat(gRatingOpt).isPresent();
        assertThat(gRatingOpt.get().getId()).isEqualTo(1);

        Optional<MpaRating> unknownMpaOpt = mpaRepository.findById(999);
        assertThat(unknownMpaOpt).isEmpty();
    }
}
