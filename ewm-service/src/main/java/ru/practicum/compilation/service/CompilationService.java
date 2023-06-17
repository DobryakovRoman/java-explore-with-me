package ru.practicum.compilation.service;

import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto addCompilation(NewCompilationDto compilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest compilationDto);

    void deleteCompilationById(Long compId);

    List<CompilationDto> getCompilations(String pinned, Integer from, Integer size);

    CompilationDto getCompilationById(Long compId);
}
