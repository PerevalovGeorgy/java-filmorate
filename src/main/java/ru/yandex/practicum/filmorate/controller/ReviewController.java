package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("Получен запрос POST /reviews на добавление отзыва");
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        log.info("Получен запрос PUT /reviews на обновление отзыва");
        return reviewService.update(review);
    }

    @GetMapping("/{id}")
    public Review findById(@PathVariable Integer id) {
        log.info("Получен запрос GET /reviews/{} на получение отзыва", id);
        return reviewService.findById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        log.info("Получен запрос DELETE /reviews/{} на удаление отзыва", id);
        reviewService.delete(id);
    }

    @GetMapping
    public Collection<Review> findReviews(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен запрос GET /reviews с параметрами: filmId={}, count={}", filmId, count);
        return reviewService.findReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос PUT /reviews/{}/like/{} (добавление лайка)", id, userId);
        reviewService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос DELETE /reviews/{}/like/{} (удаление лайка)", id, userId);
        reviewService.deleteLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос PUT /reviews/{}/dislike/{} (добавление дизлайка)", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос DELETE /reviews/{}/dislike/{} (удаление дизлайка)", id, userId);
        reviewService.deleteDislike(id, userId);
    }
}