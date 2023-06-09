package ru.practicum.request.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.practicum.request.model.ParticipationRequest;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventRequestStatusUpdateResult {
    final List<ParticipationRequestDto> confirmedRequests;
    final List<ParticipationRequestDto> rejectedRequests;

    public EventRequestStatusUpdateResult() {
        confirmedRequests = new ArrayList<>();
        rejectedRequests = new ArrayList<>();
    }

    public void addRequest(ParticipationRequest request) {
        String created = request.getCreated();
        Long event = request.getEvent().getId();
        Long id = request.getId();
        Long requester = request.getRequester();
        String status = request.getStatus();
        ParticipationRequestDto result = new ParticipationRequestDto(created, event, id, requester, status);
        if ("CONFIRMED".equals(status)) {
            confirmedRequests.add(result);
        } else if ("REJECTED".equals(status)) {
            rejectedRequests.add(result);
        }
    }
}
