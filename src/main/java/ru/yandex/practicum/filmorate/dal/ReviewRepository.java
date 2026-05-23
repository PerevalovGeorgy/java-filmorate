package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;


@Repository
public class ReviewRepository extends BaseRepository<Review> {

    public ReviewRepository(JdbcTemplate jdbcTemplate, ReviewRowMapper reviewRowMapper) {
        super(jdbcTemplate, reviewRowMapper);
    }


    public Optional<Review> findById(Integer id) {
        String sqlQuery = "SELECT * FROM reviews WHERE id = ?";
        try {
            Review review = jdbc.queryForObject(sqlQuery, mapper, id);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Review create(Review review) {

        String sqlQuery = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
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

    public Review update(Review review) {
        String sqlQuery = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbc.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );
        return findById(review.getReviewId()).orElse(review);
    }

    public Collection<Review> findReviews(Integer filmId, Integer count) {
        if (filmId == null) {
            String sqlQuery = "SELECT * FROM reviews " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbc.query(sqlQuery, mapper, count);
        } else {
            String sqlQuery = "SELECT * FROM reviews " +
                    "WHERE film_id = ? " +
                    "ORDER BY useful DESC " +
                    "LIMIT ?";
            return jdbc.query(sqlQuery, mapper, filmId, count);
        }
    }

    public boolean delete(Integer id) {
        String sqlQuery = "DELETE FROM reviews WHERE id = ?";
        int rowDelete = jdbc.update(sqlQuery, id);
        return rowDelete > 0;
    }

    public void addLikeReview(Integer reviewId, Integer userId) {
        Optional<Boolean> likeStatus = getLikeStatus(reviewId, userId);

        if (likeStatus.isEmpty()) {
            String sqlInsert = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, TRUE)";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
            jdbc.update(sqlInsert, reviewId, userId);
            jdbc.update(sqlUpdateUseful, reviewId);

        } else if (! likeStatus.get()) {
            String sqlUpdateLike = "UPDATE review_likes SET is_like = TRUE WHERE review_id = ? AND user_id = ?";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful + 2 WHERE id = ?";
            jdbc.update(sqlUpdateLike, reviewId, userId);
            jdbc.update(sqlUpdateUseful, reviewId);
        }
    }

    public void deleteLikeReview(Integer reviewId, Integer userId) {
        String sqlDelete = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = TRUE";
        String sqlUpdate = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";

        int rowsAffected = jdbc.update(sqlDelete, reviewId, userId);
        if (rowsAffected > 0) {
            jdbc.update(sqlUpdate, reviewId);
        }
    }

    public void addDislikeReview(Integer reviewId, Integer userId) {
        Optional<Boolean> likeStatus = getLikeStatus(reviewId, userId);

        if (likeStatus.isEmpty()) {
            String sqlInsert = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, FALSE)";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
            jdbc.update(sqlInsert, reviewId, userId);
            jdbc.update(sqlUpdateUseful, reviewId);

        } else if (likeStatus.get()) {
            String sqlUpdateLike = "UPDATE review_likes SET is_like = FALSE WHERE review_id = ? AND user_id = ?";
            String sqlUpdateUseful = "UPDATE reviews SET useful = useful - 2 WHERE id = ?";
            jdbc.update(sqlUpdateLike, reviewId, userId);
            jdbc.update(sqlUpdateUseful, reviewId);
        }
    }

    public void deleteDislikeReview(Integer reviewId, Integer userId) {
        String sqlDelete = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = FALSE";
        String sqlUpdate = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";

        int rowsAffected = jdbc.update(sqlDelete, reviewId, userId);
        if (rowsAffected > 0) {
            jdbc.update(sqlUpdate, reviewId);
        }
    }

    private Optional<Boolean> getLikeStatus(Integer reviewId, Integer userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        try {
            Boolean isLike = jdbc.queryForObject(sql, Boolean.class, reviewId, userId);
            return Optional.ofNullable(isLike);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
