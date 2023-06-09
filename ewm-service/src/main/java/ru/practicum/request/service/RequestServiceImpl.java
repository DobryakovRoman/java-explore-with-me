package ru.practicum.request.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.WrongDataException;
import ru.practicum.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestServiceImpl implements RequestService {

    final EventRepository eventRepository;
    final RequestDtoMapper requestDtoMapper;
    final UserService userService;
    final RequestRepository requestRepository;

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsDto(Long userId, Long eventId) {
        List<ParticipationRequest> requests = getParticipationRequests(userId, eventId);
        return requests.stream()
                .map(requestDtoMapper::mapRequestToDto)
                .collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult updateParticipationRequest(Long userId,
                                                                     Long eventId,
                                                                     EventRequestStatusUpdateRequest updateRequest) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        Event event = getEventById(eventId);
        List<ParticipationRequest> requests = getParticipationRequestsByEventId(eventId);
        if (participationLimitIsFull(event, requests)) {
            throw new ConflictException("Лимит заявок, невозможно обновить событие " + eventId);
        }
        for (ParticipationRequest request : requests) {
            if (updateRequest.getRequestIds().contains(request.getId())) {
                request.setStatus(updateRequest.getStatus());
            }
        }
        for (ParticipationRequest request : requests) {
            if (request.getStatus().equals("CONFIRMED") || request.getStatus().equals("REJECTED") || request.getStatus().equals("PENDING")) {
                result.addRequest(request);
                requestRepository.save(request);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                eventRepository.save(event);
            } else {
                throw new WrongDataException("Неверный статус запроса");
            }
        }
        return result;
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId) {
        User user = userService.getUserById(userId);
        return requestRepository.findByUserId(userId).stream()
                .map(requestDtoMapper::mapRequestToDto)
                .collect(Collectors.toList());
    }

    boolean participationLimitIsFull(Event event, List<ParticipationRequest> requests) {
        Integer confirmedRequestsCounter = 0;
        for (ParticipationRequest request : requests) {
            if (request.getStatus().equals("ACCEPTED") || request.getStatus().equals("CONFIRMED")) {
                confirmedRequestsCounter += 1;
            }
        }
        if (event.getParticipantLimit() != 0 && event.getParticipantLimit() <= confirmedRequestsCounter) {
            throw new ConflictException("Слишком много заявок на участие");
        }
        return false;

    }

    List<ParticipationRequest> getParticipationRequests(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new WrongDataException("Пользователь " + userId + " не инициатор события " + eventId);
        }
        return requestRepository.findByEventInitiatorId(userId);
    }

    List<ParticipationRequest> getParticipationRequestsByEventId(Long eventId) {
        Event event = getEventById(eventId);
        return requestRepository.findByEventId(eventId);
    }

    Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие " + eventId + " не найдено"));
    }


    @Override
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = getEventById(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Владелец не может подать заявку на участие в своём событии");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие не опубликовано, заявка не подана");
        }
        List<ParticipationRequest> requests = getParticipationRequestsByEventId(event.getId());
        if (participationLimitIsFull(event, requests)) {
            throw new ConflictException("Достигнут лимит заявок, заявка не подана");
        }
        for (ParticipationRequest request : requests) {
            if (request.getRequester().equals(userId)) {
                throw new ConflictException("Оставить заявку повторно невозможно");
            }
        }

        ParticipationRequest newRequest = ParticipationRequest.builder()
                .requester(userId)
                .created(LocalDateTime.now().toString())
                .status(event.getParticipantLimit() == 0 ? "CONFIRMED" : "PENDING")
                .event(event)
                .build();
        if (event.getRequestModeration().equals(false)) {
            newRequest.setStatus("ACCEPTED");
        }
        return requestDtoMapper.mapRequestToDto(requestRepository.save(newRequest));
    }

    @Override
    public ParticipationRequestDto cancelParticipationRequest(Long userId, Long requestId) {
        User user = userService.getUserById(userId);
        ParticipationRequest request = requestRepository.findById(requestId).orElseThrow(
                () -> new NotFoundException("Запрос " + requestId + " не существует")
        );
        if (!request.getRequester().equals(userId)) {
            throw new ConflictException("Заявка " + requestId + " оставлена не пользователем " + userId);
        }
        request.setStatus("CANCELED");
        log.info("Отмена заявки на участие " + requestId);
        return requestDtoMapper.mapRequestToDto(requestRepository.save(request));
    }
}
