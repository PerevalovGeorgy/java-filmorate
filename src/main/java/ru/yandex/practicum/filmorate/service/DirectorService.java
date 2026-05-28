package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;
    private final DirectorMapper directorMapper;

    public Collection<DirectorDto> findAll() {
        log.info("Запрос на получение списка всех режиссеров");
        return directorRepository.findAll().stream()
                .map(directorMapper::toDto)
                .collect(Collectors.toList());
    }

    public DirectorDto findById(Integer id) {
        log.info("Запрос на получение режиссер с id={}", id);
        return directorRepository.findById(id)
                .map(directorMapper::toDto)
                .orElseThrow(() -> {
                    log.warn("Режиссер с id={} не найден", id);
                    return new NotFoundException("Пользователь с id " + id + " не найден");
                });
    }

    public DirectorDto create(DirectorDto directorDto) {
        log.info("Запрос на создание режиссера с именем: {}", directorDto.getName());
        validateDirectorName(directorDto);
        Director director = directorMapper.toModel(directorDto);
        Director createddirector = directorRepository.create(director);
        return directorMapper.toDto(createddirector);
    }

    public DirectorDto update(DirectorDto directorDto) {
        log.info("Запрос на обновление режиссера с id={}", directorDto.getId());
        validationOnUpdateDirector(directorDto);
        Director director = directorMapper.toModel(directorDto);
        Director updatedDirector = directorRepository.update(director);
        return directorMapper.toDto(updatedDirector);
    }

    public DirectorDto delete(Integer directorId) {
        log.info("Запрос на удаление режиссера с id={}",  directorId);
        validationOnDeletionDirector(directorId);
        DirectorDto deletedDirector = findById(directorId);
        directorRepository.deleteById(directorId);

        return deletedDirector;
    }

    public void checkDirectorExists(Integer id) {
        if (!directorRepository.existsById(id)) {
            log.warn("Режиссер с id={} не существует", id);
            throw new NotFoundException("Режиссер с id " + id + " не найден");
        }
    }

    public void validateDirectorName (DirectorDto directorDto) {
        if (directorDto.getName() == null || directorDto.getName().trim().isEmpty()) {
            throw new ValidationException("Имя режиссера не может быть пустым");
        }
    }

    public void validationOnDeletionDirector (Integer directorId) {
        if (!directorRepository.existsById(directorId)) {
            log.warn("Попытка удалениея несуществующего режиссера с id={}",  directorId);
            throw new NotFoundException("Режиссер с id " + directorId + " не найден");
        }
    }

    public void validationOnUpdateDirector(DirectorDto directorDto) {
        validateDirectorName(directorDto);
        if (directorDto.getId() == null || !directorRepository.existsById(directorDto.getId())) {
            log.warn("Попытка обновления несуществующего режиссера с id={}", directorDto.getId());
            throw new NotFoundException("Режиссер с id " + directorDto.getId() + " не найден");
        }
    }

    public void validateDirectorsByIds(Collection<DirectorDto> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        Set<Integer> requestedIds = directors.stream()
                .map(DirectorDto::getId)
                .collect(Collectors.toSet());

        Set<Integer> existingIds = directorRepository.findExistingIds(requestedIds);

        if (existingIds.size() != requestedIds.size()) {
            Set<Integer> notFoundIds = new HashSet<>(requestedIds);
            notFoundIds.removeAll(existingIds);
            throw new NotFoundException("Режиссёры с ID " + notFoundIds + " не найдены");
        }
    }
}
