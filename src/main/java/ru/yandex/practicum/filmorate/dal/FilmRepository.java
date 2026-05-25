package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private final JdbcTemplate jdbc;

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    public Collection<Film> findAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id";
        Collection<Film> films = findMany(sql);
        loadGenresForFilms(films);
        loadDirectorForFilms(films);
        return films;
    }

    public Optional<Film> findById(Integer id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "WHERE f.id = ?";
        Optional<Film> filmOpt = findOne(sql, id);
        filmOpt.ifPresent(film -> {
            film.setGenres(getGenresByFilmId(id));
            film.setDirector(getDirectorByFilmId(id));
        });
        return filmOpt;
    }

    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        Integer mpaId = (film.getMpa() != null) ? film.getMpa().getId() : null;
        int id = insert(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), mpaId);
        film.setId(id);
        saveGenres(film);
        saveDirector(film);
        return film;
    }

    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "WHERE id = ?";
        Integer mpaId = (film.getMpa() != null) ? film.getMpa().getId() : null;
        update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), mpaId, film.getId());

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        jdbc.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());

        saveGenres(film);
        saveDirector(film);
        return film;
    }

    public void deleteFilm(Integer filmId) {
        update("DELETE FROM films WHERE id = ?", filmId);
    }

    public boolean existsById(Integer id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM films WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void addLike(Integer filmId, Integer userId) {
        update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    public Collection<Film> getLikedFilmsByUser(Integer userId) {
        Collection<Film> likedFilms = findMany("SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE fl.user_id = ? ", userId);
        loadGenresForFilms(likedFilms);
        loadDirectorForFilms(likedFilms);
        return likedFilms;
    }

    public void removeLike(Integer filmId, Integer userId) {
        update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                "ORDER BY likes_count DESC LIMIT ?";
        Collection<Film> popular = findMany(sql, count);
        loadGenresForFilms(popular);
        loadDirectorForFilms(popular);
        return popular;
    }

    public Collection<Film> getFilmsByDirectorId(Integer directorId, String sortBy) {
        String sql;
        if ("year".equalsIgnoreCase(sortBy)) {
            sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date ASC";
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                    "m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                    "ORDER BY likes_count DESC";
        } else {
            throw new IllegalArgumentException("Некорректный параметр сортировки: " + sortBy);
        }

        Collection<Film> films = findMany(sql, directorId);
        loadGenresForFilms(films);
        loadDirectorForFilms(films);
        return films;
    }

    public Collection<Film> getPopularFilmsByGenreAndYear(Integer count, Integer genreId, Integer year) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "JOIN film_genres fg ON f.id = fg.film_id " +
                "WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ? " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                "ORDER BY likes_count DESC LIMIT ?";

        Collection<Film> popular = findMany(sql, genreId, year, count);

        loadGenresForFilms(popular);
        loadDirectorForFilms(popular);

        return popular;
    }

    public Collection<Film> getPopularFilmsByYear(Integer count, Integer year) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE EXTRACT(YEAR FROM f.release_date) = ? " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                "ORDER BY likes_count DESC LIMIT ?";
        Collection<Film> popular = findMany(sql, year, count);
        loadGenresForFilms(popular);
        loadDirectorForFilms(popular);
        return popular;
    }

    public Collection<Film> getPopularFilmsByGenre(Integer count, Integer genreId) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, " +
                "m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "JOIN film_genres fg ON f.id = fg.film_id " +
                "WHERE fg.genre_id = ? " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
                "ORDER BY likes_count DESC LIMIT ?";
        Collection<Film> popular = findMany(sql, genreId, count);
        loadGenresForFilms(popular);
        loadDirectorForFilms(popular);
        return popular;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Genre> genreList = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId());
                ps.setInt(2, genreList.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genreList.size();
            }
        });
    }

    private void saveDirector(Film film) {
        if (film.getDirector() == null || film.getDirector().isEmpty()) return;
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
        List<Director> directorList = new ArrayList<>(film.getDirector());
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId());
                ps.setInt(2, directorList.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return directorList.size();
            }
        });
    }

    private LinkedHashSet<Genre> getGenresByFilmId(Integer filmId) {
        String sql = "SELECT fg.genre_id, g.name AS genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY fg.genre_id";
        return jdbc.query(sql, (rs) -> {
            LinkedHashSet<Genre> genres = new LinkedHashSet<>();
            while (rs.next()) {
                genres.add(Genre.builder()
                        .id(rs.getInt("genre_id"))
                        .name(rs.getString("genre_name"))
                        .build());
            }
            return genres;
        }, filmId);
    }

    private LinkedHashSet<Director> getDirectorByFilmId(Integer filmId) {
        String sql = "SELECT fd.director_id, d.name AS director_name " +
                "FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id = ?";
        return jdbc.query(sql, (rs) -> {
            LinkedHashSet<Director> director = new LinkedHashSet<>();
            while (rs.next()) {
                director.add(Director.builder()
                        .id(rs.getInt("director_id"))
                        .name(rs.getString("director_name"))
                        .build());
            }
            return director;
        }, filmId);
    }

    private void loadGenresForFilms(Collection<Film> films) {
        if (films.isEmpty()) return;
        String sql = "SELECT fg.film_id, fg.genre_id, g.name AS genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "ORDER BY fg.genre_id";
        Map<Integer, LinkedHashSet<Genre>> map = new HashMap<>();
        jdbc.query(sql, (rs) -> {
            int fId = rs.getInt("film_id");
            Genre genre = Genre.builder()
                    .id(rs.getInt("genre_id"))
                    .name(rs.getString("genre_name"))
                    .build();
            map.computeIfAbsent(fId, k -> new LinkedHashSet<>()).add(genre);
        });
        films.forEach(f -> f.setGenres(map.getOrDefault(f.getId(), new LinkedHashSet<>())));
    }

    private void loadDirectorForFilms(Collection<Film> films) {
        if (films.isEmpty()) return;
        String sql = "SELECT fd.film_id, fd.director_id, d.name AS director_name " +
                "FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.id";
        Map<Integer, LinkedHashSet<Director>> map = new HashMap<>();
        jdbc.query(sql, (rs) -> {
            int fId = rs.getInt("film_id");
            Director director = Director.builder()
                    .id(rs.getInt("director_id"))
                    .name(rs.getString("director_name"))
                    .build();
            map.computeIfAbsent(fId, k -> new LinkedHashSet<>()).add(director);
        });
        films.forEach(f -> f.setDirector(map.getOrDefault(f.getId(), new LinkedHashSet<>())));
    }

    public Collection<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id," +
                " mr.name as mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?) " +
                "  AND f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?) " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id " +
                "ORDER BY COUNT(fl.user_id) DESC";
        Collection<Film> films = findMany(sql, userId, friendId);
        loadGenresForFilms(films);
        loadDirectorForFilms(films);
        return films;
    }

}
