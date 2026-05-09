package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipStatusRowMapper implements RowMapper<FriendshipStatus> {

    @Override
    public FriendshipStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FriendshipStatus.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .build();
    }
}
