package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.MpaRatingDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

@Component
public class MpaRatingMapper {

    public MpaRatingDto toDto(MpaRating mpaRating) {
        if (mpaRating == null) {
            return null;
        }
        return MpaRatingDto.builder()
                .id(mpaRating.getId())
                .name(mpaRating.getName())
                .build();
    }

    public MpaRating toModel(MpaRatingDto mpaRatingDto) {
        if (mpaRatingDto == null) {
            return null;
        }
        return MpaRating.builder()
                .id(mpaRatingDto.getId())
                .name(mpaRatingDto.getName())
                .build();
    }
}
