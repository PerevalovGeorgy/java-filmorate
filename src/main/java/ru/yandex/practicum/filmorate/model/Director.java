package ru.yandex.practicum.filmorate.model;

import lombok.*;


@Data
@EqualsAndHashCode(of = {"id"})
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Director {
    private Integer id;
    private String name;
}
