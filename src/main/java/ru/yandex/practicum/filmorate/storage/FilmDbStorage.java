package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private static final String BASE_FILM_SELECT =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, " +
                    "m.id AS mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id ";

    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = jdbcTemplate.query(BASE_FILM_SELECT, filmRowMapper);
        loadGenresForFilms(films); // Пакетная загрузка жанров в 1 дополнительный запрос
        return films;
    }

    @Override
    public Optional<Film> findById(Integer id) {
        String sql = BASE_FILM_SELECT + "WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film != null) {
                loadGenresForFilms(Collections.singletonList(film));
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        updateFilmGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        updateFilmGenres(film);
        return findById(film.getId()).orElse(film);
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void addLike(Integer filmId, Integer userId) {
        String sql = "MERGE INTO film_likes KEY (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Integer filmId, Integer userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        String sql = BASE_FILM_SELECT +
                "LEFT JOIN film_likes l ON f.id = l.film_id " +
                "GROUP BY f.id, m.id, m.name " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";
        Collection<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        loadGenresForFilms(films);
        return films;
    }

    @Override
    public void deleteFilm(Integer id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    private void updateFilmGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());
        jdbcTemplate.batchUpdate(insertSql, batchArgs);
    }

    private void loadGenresForFilms(Collection<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sql = "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + inSql + ") " +
                "ORDER BY g.id ASC";

        Object[] filmIds = films.stream().map(Film::getId).toArray();

        jdbcTemplate.query(sql, (ResultSet rs) -> {
            while (rs.next()) {
                Integer filmId = rs.getInt("film_id");
                Genre genre = Genre.builder()
                        .id(rs.getInt("genre_id"))
                        .name(rs.getString("genre_name"))
                        .build();

                Film film = filmMap.get(filmId);
                if (film != null) {
                    film.getGenres().add(genre);
                }
            }
            return null;
        }, filmIds);
    }
}
