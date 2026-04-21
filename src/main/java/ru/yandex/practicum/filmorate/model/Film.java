package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Film {

    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private long duration;
    private Set<Integer> likes;

    @Builder.Default
    private Set<Genre> genres;
    private MpaRating mpaRating;

    public void addLike(Integer userId) {
        if (likes == null) {
            likes = new HashSet<>();
        }
        likes.add(userId);
    }

    public void removeLike(Integer userId) {
        if (likes != null) {
            likes.remove(userId);
        }
    }

    public int getLikesCount() {
        return likes != null ? likes.size() : 0;
    }

}