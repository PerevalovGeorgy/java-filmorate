package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
public class FilmRepository extends BaseRepository<Film> {

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Film> findAll() {
        String sql = "SELECT f.*, m.mpaRating FROM films f LEFT JOIN MpaRating m ON f.mpaRating_id = m.id";
        Collection<Film> films = findMany(sql);
        loadGenresForFilms(films);
        return films;
    }

    public Optional<Film> findById(Integer id) {
        String sql = "SELECT f.*, m.mpaRating FROM films f LEFT JOIN MpaRating m ON f.mpaRating_id = m.id WHERE f.id = ?";
        Optional<Film> filmOpt = findOne(sql, id);
        filmOpt.ifPresent(film -> film.setGenres(getGenresByFilmId(id)));
        return filmOpt;
    }

    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, releaseDate, duration, mpaRating_id) VALUES (?, ?, ?, ?, ?)";
        Integer mpaId = (film.getMpa() != null) ? film.getMpa().getId() : null;

        int id = insert(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), mpaId);
        film.setId(id);
        saveGenres(film);
        return film;
    }

    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, mpaRating_id = ? WHERE id = ?";
        Integer mpaId = (film.getMpa() != null) ? film.getMpa().getId() : null;

        update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), mpaId, film.getId());

        jdbc.update("DELETE FROM FilmsGenre WHERE filmId = ?", film.getId());
        saveGenres(film);
        return film;
    }

    public boolean existsById(Integer id) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM films WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    public void addLike(Integer filmId, Integer userId) {
        update("INSERT INTO filmLikes (filmID, userId) VALUES (?, ?)", filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        update("DELETE FROM filmLikes WHERE filmID = ? AND userId = ?", filmId, userId);
    }

    public Collection<Film> getPopular(Integer count) {
        String sql = "SELECT f.*, m.mpaRating, COUNT(fl.userId) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN MpaRating m ON f.mpaRating_id = m.id " +
                "LEFT JOIN filmLikes fl ON f.id = fl.filmID " +
                "GROUP BY f.id, m.mpaRating " +
                "ORDER BY likes_count DESC LIMIT ?";
        Collection<Film> popular = findMany(sql, count);
        loadGenresForFilms(popular);
        return popular;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        String sql = "INSERT INTO FilmsGenre (filmId, genreId) VALUES (?, ?)";
        List<Genre> genreList = new ArrayList<>(film.getGenres());
        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setInt(1, film.getId());
                ps.setInt(2, genreList.get(i).getId());
            }
            @Override
            public int getBatchSize() { return genreList.size(); }
        });
    }

    private LinkedHashSet<Genre> getGenresByFilmId(Integer filmId) {
        String sql = "SELECT fg.genreId FROM FilmsGenre fg WHERE fg.filmId = ? ORDER BY fg.genreId";
        List<Integer> ids = jdbc.query(sql, (rs, rowNum) -> rs.getInt("genreId"), filmId);
        LinkedHashSet<Genre> genres = new LinkedHashSet<>();
        ids.forEach(id -> genres.add(Genre.valueOf(id)));
        return genres;
    }

    private void loadGenresForFilms(Collection<Film> films) {
        if (films.isEmpty()) return;
        String sql = "SELECT fg.filmId, fg.genreId FROM FilmsGenre fg ORDER BY fg.genreId";
        Map<Integer, LinkedHashSet<Genre>> map = new HashMap<>();
        jdbc.query(sql, (rs) -> {
            int fId = rs.getInt("filmId");
            int gId = rs.getInt("genreId");
            map.computeIfAbsent(fId, k -> new LinkedHashSet<>()).add(Genre.valueOf(gId));
        });
        films.forEach(f -> f.setGenres(map.getOrDefault(f.getId(), new LinkedHashSet<>())));
    }
}
