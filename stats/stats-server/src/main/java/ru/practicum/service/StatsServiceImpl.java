package ru.practicum.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.model.Stats;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class StatsServiceImpl implements StatsService {

    final StatsRepository statsRepository;

    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        return DtoMapper.toEndpointHitDto(statsRepository.save(DtoMapper.toEndpointHit(endpointHitDto)));
    }

    public List<StatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Дата и время начала не должна быть позже даты и времени конца");
        }
        if (unique) {
            if (uris != null) {
                return statsRepository.findUniqueStats(start, end, uris).stream()
                        .map(DtoMapper::toStatsDto)
                        .collect(Collectors.toList());
            }
            return statsRepository.findUniqueStats(start, end).stream()
                    .map(DtoMapper::toStatsDto)
                    .collect(Collectors.toList());
        }
        if (uris != null) {
            List<Stats> stats = statsRepository.findStats(start, end, uris);
            return stats.stream()
                    .map(DtoMapper::toStatsDto)
                    .collect(Collectors.toList());
        }
        return statsRepository.findStats(start, end).stream()
                .map(DtoMapper::toStatsDto)
                .collect(Collectors.toList());
    }
}
