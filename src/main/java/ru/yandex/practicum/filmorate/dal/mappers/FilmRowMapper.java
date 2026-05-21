package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashSet;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        MpaRating mpa = MpaRating.builder()
                .id(rs.getInt("mpa_rating_id"))
                .name(rs.getString("mpa_name"))
                .build();

        return Film.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getObject("release_date", LocalDate.class))
                .duration(rs.getLong("duration"))
                .mpa(mpa.getId() == 0 ? null : mpa)
                .genres(new LinkedHashSet<>())
                .directors(new LinkedHashSet<>())
                .build();
    }
}
