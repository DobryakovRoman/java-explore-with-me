package ru.practicum.request.service;

import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;

import java.util.List;

public class EventRequestStatusUpdateResultMapper {
    public static EventRequestStatusUpdateResult mapToEventRequestStatusUpdateResult(List<ParticipationRequestDto> confirmedRequests,
                                                                                     List<ParticipationRequestDto> rejectedRequests) {
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests)
                .rejectedRequests(rejectedRequests)
                .build();
    }
}
