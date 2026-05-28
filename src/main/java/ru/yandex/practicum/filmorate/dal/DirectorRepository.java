package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DirectorRepository extends BaseRepository<Director> {
    public DirectorRepository(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Director> findAll() {
        return findMany("SELECT * FROM directors");
    }

    public Optional<Director> findById(Integer id) {
        return findOne("SELECT * FROM directors WHERE id = ?", id);
    }

    public Director create(Director director) {
        String sql = "INSERT INTO directors ( name ) VALUES (?)";
        int id = insert(sql, director.getName());
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        update(sql, director.getName(), director.getId());
        return director;
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public void deleteById(Integer id) {
        delete("DELETE FROM directors WHERE id = ?", id);
    }

    public Set<Integer> findExistingIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptySet();
        }

        String sql = "SELECT id FROM directors WHERE id IN (" +
                ids.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";

        List<Integer> existingIds = jdbc.queryForList(sql, Integer.class);
        return new HashSet<>(existingIds);
    }

    public boolean existsAllByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        Set<Integer> existingIds = findExistingIds(ids);
        return existingIds.size() == ids.size();
    }
}

