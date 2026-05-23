package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {


    Optional<Review> findById(Integer id);

    Review create(Review review);

    Review update(Review review);

    Collection<Review> findReviews(Integer filmId, Integer count);

    boolean delete(Integer id);

    void addLikeReview(Integer reviewId, Integer userId);

    void deleteLikeReview(Integer reviewId, Integer userId);

    void addDislikeReview(Integer reviewId, Integer userId);

    void deleteDislikeReview(Integer reviewId, Integer userId);

}
