package ru.practicum.request.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.ArrayList;
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
    @Transactional
    public EventRequestStatusUpdateResult updateParticipationRequest(Long userId,
                                                                     Long eventId,
                                                                     EventRequestStatusUpdateRequest updateRequest) {
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
        Event event = getEventById(eventId);
        List<ParticipationRequest> requests = getParticipationRequestsByEventId(eventId);
        Long confirmedRequestsCounter = requests.stream().filter(r -> r.getStatus().equals("CONFIRMED")).count();
        List<ParticipationRequest> result = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            if (request.getStatus().equals("CONFIRMED") || request.getStatus().equals("REJECTED") || request.getStatus().equals("PENDING")) {
                if (updateRequest.getStatus().equals("CONFIRMED") && event.getParticipantLimit() != 0) {
                    if (event.getParticipantLimit() < confirmedRequestsCounter) {
                        List<ParticipationRequest> pending = requestRepository.findRequestByEventIdAndStatus(event.getId(), "PENDING").stream()
                                .peek(p -> p.setStatus("REJECTED"))
                                .collect(Collectors.toList());
                        requestRepository.saveAll(pending);
                        log.info("Слишком много заявок на участие, оставшиеся заявки переведены в статус Reject");
                        throw new ConflictException("Слишком много заявок на участие, оставшиеся заявки переведены в статус Reject");
                    }
                }
                if (updateRequest.getStatus().equals("REJECTED") && request.getStatus().equals("CONFIRMED")) {
                    throw new ConflictException("Нельзя отменять подтверждённую заявку");
                }
                request.setStatus(updateRequest.getStatus());
                ParticipationRequestDto participationRequestDto = requestDtoMapper.mapRequestToDto(request);
                if ("CONFIRMED".equals(participationRequestDto.getStatus())) {
                    confirmedRequests.add(participationRequestDto);
                } else if ("REJECTED".equals(participationRequestDto.getStatus())) {
                    rejectedRequests.add(participationRequestDto);
                }
                result.add(request);
                confirmedRequestsCounter++;
            } else {
                throw new WrongDataException("Неверный статус запроса");
            }
        }
        requestRepository.saveAll(result);
        return EventRequestStatusUpdateResultMapper.mapToEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    public List<ParticipationRequestDto> getParticipationRequestsByUserId(Long userId) {
        User user = userService.getUserById(userId);
        return requestRepository.findByUserId(userId).stream()
                .map(requestDtoMapper::mapRequestToDto)
                .collect(Collectors.toList());
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
        if (participationLimitIsFull(event)) {
            throw new ConflictException("Достигнут лимит заявок, заявка не подана");
        }
        for (ParticipationRequest request : requests) {
            if (request.getRequester().getId().equals(userId)) {
                throw new ConflictException("Оставить заявку повторно невозможно");
            }
        }

        ParticipationRequest newRequest = ParticipationRequest.builder()
                .requester(user)
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
        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Заявка " + requestId + " оставлена не пользователем " + userId);
        }
        request.setStatus("CANCELED");
        log.info("Отмена заявки на участие " + requestId);
        return requestDtoMapper.mapRequestToDto(requestRepository.save(request));
    }


    boolean participationLimitIsFull(Event event) {
        Long confirmedRequestsCounter = requestRepository.countByEventAndStatuses(event.getId(), List.of("CONFIRMED", "ACCEPTED"));
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
}
