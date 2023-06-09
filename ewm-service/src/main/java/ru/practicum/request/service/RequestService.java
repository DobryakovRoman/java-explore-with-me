package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getParticipationRequestsDto(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateParticipationRequest(Long userId,
                                                              Long eventId,
                                                              EventRequestStatusUpdateRequest updateRequest);

    List<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId);

}
