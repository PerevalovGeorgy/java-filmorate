package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         @Qualifier("filmRepository") FilmStorage filmStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage) {

        this.reviewStorage = reviewStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    private Review getReviewOrThrow(Integer reviewId) {
        return reviewStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewId + " не найден"));
    }

    private Film getFilmOrThrow(Integer filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    private User getUserOrThrow(Integer userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    public Review create(Review review) {
        getUserOrThrow(review.getUserId());
        getFilmOrThrow(review.getFilmId());

        return reviewStorage.create(review);
    }

    public Review findById(Integer id) {
        return getReviewOrThrow(id);
    }

    public Review update(Review review) {
        getReviewOrThrow(review.getReviewId());

        return reviewStorage.update(review);
    }

    public void delete(Integer id) {
        getReviewOrThrow(id);
        reviewStorage.delete(id);
    }

    public Collection<Review> findReviews(Integer filmId, Integer count) {
        if (filmId != null) {
            getFilmOrThrow(filmId);
        }
        return reviewStorage.findReviews(filmId, count);
    }

    public void addLike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewStorage.addLikeReview(id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewStorage.deleteLikeReview(id, userId);
    }

    public void addDislike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewStorage.addDislikeReview(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewStorage.deleteDislikeReview(id, userId);
    }
}
