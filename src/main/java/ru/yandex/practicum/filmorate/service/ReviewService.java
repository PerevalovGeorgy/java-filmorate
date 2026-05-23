package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Slf4j
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            FilmRepository filmRepository,
            UserRepository userRepository
    ) {

        this.reviewRepository = reviewRepository;
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    private Review getReviewOrThrow(Integer reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewId + " не найден"));
    }

    private Film getFilmOrThrow(Integer filmId) {
        return filmRepository.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }

    private User getUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    public Review create(Review review) {
        getUserOrThrow(review.getUserId());
        getFilmOrThrow(review.getFilmId());

        return reviewRepository.create(review);
    }

    public Review findById(Integer id) {
        return getReviewOrThrow(id);
    }

    public Review update(Review review) {
        getReviewOrThrow(review.getReviewId());

        return reviewRepository.update(review);
    }

    public void delete(Integer id) {
        getReviewOrThrow(id);
        reviewRepository.delete(id);
    }

    public Collection<Review> findReviews(Integer filmId, Integer count) {
        if (filmId != null) {
            getFilmOrThrow(filmId);
        }
        return reviewRepository.findReviews(filmId, count);
    }

    public void addLike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewRepository.addLikeReview(id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewRepository.deleteLikeReview(id, userId);
    }

    public void addDislike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewRepository.addDislikeReview(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        reviewRepository.deleteDislikeReview(id, userId);
    }
}
