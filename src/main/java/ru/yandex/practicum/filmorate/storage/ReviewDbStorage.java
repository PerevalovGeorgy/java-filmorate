package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Component
@Qualifier("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;

    @Override
    public Optional<Review> findById(Integer id) {
        String sqlQuery = "SELECT * FROM reviews WHERE id = ?";
        try {
            Review review = jdbcTemplate.queryForObject(sqlQuery, reviewRowMapper, id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Review create(Review review) {

        String sqlQuery = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());


            return stmt;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().intValue());

        return review;
    }

    @Override
    public Review update(Review review) {
        String sqlQuery = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return findById(review.getReviewId()).orElse(review);
    }

    @Override
    public Collection<Review> findReviews(Integer filmId, Integer count) {
        if (filmId == null) {
            String sqlQuery = "SELECT * FROM reviews " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbcTemplate.query(sqlQuery, reviewRowMapper, count);
        } else {
            String sqlQuery = "SELECT * FROM reviews " +
                    "WHERE film_id = ? " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbcTemplate.query(sqlQuery, reviewRowMapper, filmId, count);
        }
    }

    @Override
    public boolean delete(Integer id) {
        String sqlQuery = "DELETE FROM reviews WHERE id = ?";
        int rowDelete = jdbcTemplate.update(sqlQuery, id);
        return rowDelete > 0;
    }

    @Override
    public void addLikeReview(Integer reviewId, Integer userId) {
        Optional<Boolean> likeStatus = getLikeStatus(reviewId, userId);

        if (likeStatus.isEmpty()) {
            // Оценки не было: делаем обычный INSERT и прибавляем к рейтингу 1
            String sqlInsert = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, TRUE)";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
            jdbcTemplate.update(sqlInsert, reviewId, userId);
            jdbcTemplate.update(sqlUpdateUseful, reviewId);

        } else if (!likeStatus.get()) {
            // Был дизлайк: меняем его на лайк и прибавляем к рейтингу 2
            String sqlUpdateLike = "UPDATE review_likes SET is_like = TRUE WHERE review_id = ? AND user_id = ?";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful + 2 WHERE id = ?";
            jdbcTemplate.update(sqlUpdateLike, reviewId, userId);
            jdbcTemplate.update(sqlUpdateUseful, reviewId);
        }
    }

    @Override
    public void deleteLikeReview(Integer reviewId, Integer userId) {
        String sqlDelete = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = TRUE";
        String sqlUpdate = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sqlDelete, reviewId, userId);
        if (rowsAffected > 0) {
            jdbcTemplate.update(sqlUpdate, reviewId);
        }
    }

    @Override
    public void addDislikeReview(Integer reviewId, Integer userId) {
        Optional<Boolean> likeStatus = getLikeStatus(reviewId, userId);

        if (likeStatus.isEmpty()) {
            // Оценки не было: делаем обычный INSERT и вычитаем из рейтинга 1
            String sqlInsert = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, FALSE)";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
            jdbcTemplate.update(sqlInsert, reviewId, userId);
            jdbcTemplate.update(sqlUpdateUseful, reviewId);

        } else if (likeStatus.get()) {
            // Был лайк: меняем его на дизлайк и вычитаем из рейтинга 2
            String sqlUpdateLike = "UPDATE review_likes SET is_like = FALSE WHERE review_id = ? AND user_id = ?";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful - 2 WHERE id = ?";
            jdbcTemplate.update(sqlUpdateLike, reviewId, userId);
            jdbcTemplate.update(sqlUpdateUseful, reviewId);
        }
    }

    @Override
    public void deleteDislikeReview(Integer reviewId, Integer userId) {
        String sqlDelete = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = FALSE";
        String sqlUpdate = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sqlDelete, reviewId, userId);
        if (rowsAffected > 0) {
            jdbcTemplate.update(sqlUpdate, reviewId);
        }
    }

    private Optional<Boolean> getLikeStatus(Integer reviewId, Integer userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        try {
            Boolean isLike = jdbcTemplate.queryForObject(sql, Boolean.class, reviewId, userId);
            return Optional.ofNullable(isLike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }


}
