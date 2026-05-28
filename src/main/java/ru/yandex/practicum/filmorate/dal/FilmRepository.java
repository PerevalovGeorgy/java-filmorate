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
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private final JdbcTemplate jdbc;
    private static final String POPULAR_FILM_BASE = "SELECT f.id, f.name, f.description, f.release_date, " +
            "f.duration, f.mpa_rating_id, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count " +
            "FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id " +
            "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
            "%s " +
            "%s " +
            "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name " +
            "ORDER BY likes_count DESC " +
            "%s ";

    private static final String FILM_BASE = "SELECT f.id, f.name, f.description, f.release_date, " +
            "f.duration, f.mpa_rating_id, m.name AS mpa_name " +
            "FROM films f " +
            "LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id ";

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
        this.jdbc = jdbc;
    }

    public Collection<Film> findAll() {
        String sql = FILM_BASE;
        return findManyWithDetails(sql);
    }

    public Optional<Film> findById(Integer id) {
        String sql = FILM_BASE + "WHERE f.id = ?";
        return findOneWithDetails(sql, id);
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

    public boolean addLike(Integer filmId, Integer userId) {
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, filmId, userId);
        if (count > 0) {
            return false;
        }
        update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        return true;
    }

    public Collection<Film> getLikedFilmsByUser(Integer userId) {
        String sql = FILM_BASE +
                "JOIN film_likes fl ON f.id = fl.film_id " +
                "WHERE fl.user_id = ? ";
        return findManyWithDetails(sql, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        update("DELETE FROM film_likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        String sql = String.format(POPULAR_FILM_BASE, "", "", "LIMIT ? ");


        return findManyWithDetails(sql, count);
    }


    public Collection<Film> getFilmsByDirectorId(Integer directorId, String sortBy) {
        String sql;
        if ("year".equalsIgnoreCase(sortBy)) {
            sql = FILM_BASE +
                    "JOIN film_directors fd ON f.id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY EXTRACT(YEAR FROM f.release_date) ASC";
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            sql = String.format(POPULAR_FILM_BASE, "JOIN film_directors fd ON f.id = fd.film_id ",
                    "WHERE fd.director_id = ? ", "");
        } else {
            throw new IllegalArgumentException("Некорректный параметр сортировки: " + sortBy);
        }
        return findManyWithDetails(sql, directorId);
    }

    public Collection<Film> getPopularFilmsByGenreAndYear(Integer count, Integer genreId, Integer year) {
        String sql = String.format(POPULAR_FILM_BASE, "JOIN film_genres fg ON f.id = fg.film_id ",
                "WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ? ", "LIMIT ? ");

        Collection<Film> popular = findMany(sql, genreId, year, count);

        loadGenresForFilms(popular);
        loadDirectorForFilms(popular);

        return popular;
    }

    public Collection<Film> getPopularFilmsByYear(Integer count, Integer year) {
        String sql = String.format(POPULAR_FILM_BASE, "", "WHERE EXTRACT(YEAR FROM f.release_date) = ? ",
                "LIMIT ? ");

        Collection<Film> popular = findMany(sql, year, count);
        loadGenresForFilms(popular);
        loadDirectorForFilms(popular);
        return popular;
    }

    public Collection<Film> getPopularFilmsByGenre(Integer count, Integer genreId) {
        String sql = String.format(POPULAR_FILM_BASE, "JOIN film_genres fg ON f.id = fg.film_id ",
                "WHERE fg.genre_id = ? ", "LIMIT ? ");

        Collection<Film> popular = findMany(sql, genreId, count);
        loadGenresForFilms(popular);
        loadDirectorForFilms(popular);
        return popular;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
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
        if (film.getDirector() == null || film.getDirector().isEmpty()) {
            return;
        }
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

        return jdbc.query(sql, rs -> {
            LinkedHashSet<Director> directors = new LinkedHashSet<>();
            while (rs.next()) {
                directors.add(Director.builder()
                        .id(rs.getInt("director_id"))
                        .name(rs.getString("director_name"))
                        .build());
            }
            return directors;
        }, filmId);
    }

    private void loadGenresForFilms(Collection<Film> films) {
        if (films.isEmpty()) {
            return;
        }
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
        if (films == null || films.isEmpty()) {
            return;
        }
        String filmIds = films.stream()
                .map(Film::getId)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        if (filmIds.isEmpty()) {
            return;
        }

        String sql = "SELECT fd.film_id, d.id, d.name " +
                "FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id IN (" + filmIds + ")";

        Map<Integer, LinkedHashSet<Director>> directorsByFilm = new HashMap<>();

        jdbc.query(sql, rs -> {
            Integer filmId = rs.getInt("film_id");
            Director director = Director.builder()
                    .id(rs.getInt("id"))
                    .name(rs.getString("name"))
                    .build();

            directorsByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(director);
        });
        for (Film film : films) {
            LinkedHashSet<Director> directors = directorsByFilm.getOrDefault(film.getId(), new LinkedHashSet<>());
            film.setDirector(directors);
        }
    }

    public Collection<Film> getCommonFilms(Integer userId, Integer friendId) {
        String sql = String.format(POPULAR_FILM_BASE, "WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?) ",
                " AND f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?) ", "");

        return findManyWithDetails(sql, userId, friendId);
    }


    public Collection<Film> searchFilms(String query, String by) {
        boolean byTitle = by.contains("title");
        boolean byDirector = by.contains("director");

        String searchPattern = "%" + query + "%";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, ")
                .append("m.name AS mpa_name, COUNT(DISTINCT fl.user_id) AS likes_count ")
                .append("FROM films f ")
                .append("LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id ")
                .append("LEFT JOIN film_likes fl ON f.id = fl.film_id ")
                .append("LEFT JOIN film_directors fd ON f.id = fd.film_id ")
                .append("LEFT JOIN directors d ON fd.director_id = d.id ");

        List<Object> params = new ArrayList<>();

        if (byTitle && byDirector) {
            sql.append("WHERE LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?) ");
            params.add(searchPattern);
            params.add(searchPattern);
        } else if (byTitle) {
            sql.append("WHERE LOWER(f.name) LIKE LOWER(?) ");
            params.add(searchPattern);
        } else if (byDirector) {
            sql.append("WHERE LOWER(d.name) LIKE LOWER(?) ");
            params.add(searchPattern);
        } else {
            throw new IllegalArgumentException("Некорректный параметр поиска 'by': " + by);
        }

        sql.append("GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.name ")
                .append("ORDER BY likes_count DESC");

        Collection<Film> searched = findMany(sql.toString(), params.toArray());

        loadGenresForFilms(searched);
        loadDirectorForFilms(searched);

        return searched;
    }

    private Collection<Film> findManyWithDetails(String query, Object... params) {
        Collection<Film> films = findMany(query, params);
        loadGenresForFilms(films);
        loadDirectorForFilms(films);
        return films;
    }

    private Optional<Film> findOneWithDetails(String query, Object... params) {
        Optional<Film> film = findOne(query, params);
        if (film.isPresent()) {
            Film f = film.get();
            f.setGenres(getGenresByFilmId(f.getId()));
            f.setDirector(getDirectorByFilmId(f.getId()));
        }
        return film;
    }

    public Map<Integer, Set<Integer>> getLikedFilmIdsGroupedByUsers(List<Integer> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String userIdsStr = userIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String sql = "SELECT fl.user_id, fl.film_id " +
                "FROM film_likes fl " +
                "WHERE fl.user_id IN (" + userIdsStr + ")";

        Map<Integer, Set<Integer>> result = new HashMap<>();
        jdbc.query(sql, (rs) -> {
            int userId = rs.getInt("user_id");
            int filmId = rs.getInt("film_id");
            result.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        });

        return result;
    }
}
