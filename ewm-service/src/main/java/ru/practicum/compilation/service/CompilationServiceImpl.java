package ru.practicum.compilation.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventDtoMapper;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationServiceImpl implements CompilationService {
    final CompilationRepository compilationRepository;
    final CompilationDtoMapper compilationDtoMapper;
    final EventRepository eventRepository;

    @Override
    public CompilationDto addCompilation(NewCompilationDto compilationDto) {
        List<Event> events;
        if (compilationDto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(compilationDto.getEvents());
        } else {
            events = new ArrayList<>();
        }
        Compilation compilation = compilationDtoMapper.mapNewCompilationDtoToCompilation(compilationDto, events);
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
        compilation = compilationRepository.save(compilation);
        log.info("Подборка сохранена " + compilation.getId());
        CompilationDto result = compilationDtoMapper.mapCompilationToDto(compilation);
        result.setEvents(compilation.getEvents().stream().map(EventDtoMapper::mapEventToShortDto).collect(Collectors.toList()));
        return result;
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка не существует " + compId));
        List<Event> events;
        if (compilationDto.getEvents() != null) {
            events = eventRepository.findAllByIdIn(compilationDto.getEvents());
            compilation.setEvents(events);
        }
        if (compilationDto.getTitle() != null) {
            compilation.setTitle(compilationDto.getTitle());
        }
        if (compilationDto.getPinned() != null) {
            compilation.setPinned(compilationDto.getPinned());
        }
        compilation = compilationRepository.save(compilation);
        log.info("Подборка обновлена " + compId);
        CompilationDto result = compilationDtoMapper.mapCompilationToDto(compilation);
        result.setEvents(compilation.getEvents().stream()
                .map(EventDtoMapper::mapEventToShortDto)
                .collect(Collectors.toList()));
        return result;
    }

    @Override
    public void deleteCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка не существует " + compId));
        compilationRepository.deleteById(compId);
        log.info("Подборка удалена " + compId);
    }

    @Override
    public List<CompilationDto> getCompilations(String pinned, Integer from, Integer size) {
        List<Compilation> compilations;
        if (pinned.isEmpty()) {
            compilations = compilationRepository.findAll(PageRequest.of(from / size, size)).getContent();
        } else {
            Boolean pin = Boolean.parseBoolean(pinned);
            compilations = compilationRepository.findAllByPinned(pin, PageRequest.of(from / size, size));
        }
        List<CompilationDto> compilationDtos = compilations.stream()
                .map(compilationDtoMapper::mapCompilationToDto)
                .peek(cdto -> cdto.setEvents(
                                compilations.stream()
                                        .filter(c -> cdto.getId().equals(c.getId()))
                                        .findFirst()
                                        .get()
                                        .getEvents()
                                        .stream()
                                        .map(EventDtoMapper::mapEventToShortDto)
                                        .collect(Collectors.toList())
                        )
                )
                .collect(Collectors.toList());
        return compilationDtos;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException("Подборка не найдена " + compId)
        );
        CompilationDto result = compilationDtoMapper.mapCompilationToDto(compilation);
        result.setEvents(compilation.getEvents().stream()
                .map(EventDtoMapper::mapEventToShortDto)
                .collect(Collectors.toList()));
        log.info("Подборка не найдена " + compId);
        return result;
    }
}
