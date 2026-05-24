package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final FeedService feedService;

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
        Review createdReview = reviewRepository.create(review);

        feedService.logEvent(createdReview.getUserId(), "REVIEW", "ADD", createdReview.getReviewId());
        return createdReview;
    }

    public Review findById(Integer id) {
        return getReviewOrThrow(id);
    }

    public Review update(Review review) {
        Review existingReview = getReviewOrThrow(review.getReviewId());
        review.setUserId(existingReview.getUserId());
        review.setFilmId(existingReview.getFilmId());
        review.setUseful(existingReview.getUseful());

        Review updatedReview = reviewRepository.update(review);
        feedService.logEvent(updatedReview.getUserId(), "REVIEW", "UPDATE", updatedReview.getReviewId());

        return updatedReview;
    }

    public void delete(Integer id) {
        Review review = getReviewOrThrow(id);
        reviewRepository.delete(id);
        feedService.logEvent(review.getUserId(), "REVIEW", "REMOVE", review.getReviewId());
    }

    public Collection<Review> findReviews(Integer filmId, Integer count) {
        if (filmId != null) {
            getFilmOrThrow(filmId);
        }
        return reviewRepository.findReviews(filmId, count);
    }

    public void addLike(Integer id, Integer userId) {
        Review review = getReviewOrThrow(id);
        getUserOrThrow(userId);

        if (reviewRepository.hasUserLikedReview(id, userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому отзыву");
        }
        reviewRepository.addLikeReview(id, userId);
    }

    public void deleteLike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        if (!reviewRepository.hasUserLikedReview(id, userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому отзыву");
        }
        reviewRepository.deleteLikeReview(id, userId);
    }

    public void addDislike(Integer id, Integer userId) {
        Review review = getReviewOrThrow(id);
        getUserOrThrow(userId);

        if (reviewRepository.hasUserDislikedReview(id, userId)) {
            throw new ValidationException("Пользователь уже поставил дизлайк этому отзыву");
        }
        reviewRepository.addDislikeReview(id, userId);
    }

    public void deleteDislike(Integer id, Integer userId) {
        getReviewOrThrow(id);
        getUserOrThrow(userId);
        if (!reviewRepository.hasUserDislikedReview(id, userId)) {
            throw new ValidationException("Пользователь не ставил дизлайк этому отзыву");
        }
        reviewRepository.deleteDislikeReview(id, userId);
    }
}
