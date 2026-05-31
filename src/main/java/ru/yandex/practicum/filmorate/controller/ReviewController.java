package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Review> create(@Valid @RequestBody Review review) {
        log.info("Получен запрос POST /reviews на добавление отзыва");
        Review created = reviewService.create(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<Review> update(@Valid @RequestBody Review review) {
        log.info("Получен запрос PUT /reviews на обновление отзыва");
        return ResponseEntity.ok(reviewService.update(review));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> findById(@PathVariable Integer id) {
        log.info("Получен запрос GET /reviews/{} на получение отзыва", id);
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        log.info("Получен запрос DELETE /reviews/{} на удаление отзыва", id);
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Collection<Review>> findReviews(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") Integer count) {
        log.info("Получен запрос GET /reviews с параметрами: filmId={}, count={}", filmId, count);
        return ResponseEntity.ok(reviewService.findReviews(filmId, count));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос PUT /reviews/{}/like/{} (добавление лайка)", id, userId);
        reviewService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос DELETE /reviews/{}/like/{} (удаление лайка)", id, userId);
        reviewService.deleteLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос PUT /reviews/{}/dislike/{} (добавление дизлайка)", id, userId);
        reviewService.addDislike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> deleteDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("Получен запрос DELETE /reviews/{}/dislike/{} (удаление дизлайка)", id, userId);
        reviewService.deleteDislike(id, userId);
        return ResponseEntity.ok().build();
    }
}