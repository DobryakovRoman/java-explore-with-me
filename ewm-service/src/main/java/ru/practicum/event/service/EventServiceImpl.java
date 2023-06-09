package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.dto.*;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.WrongDataException;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventServiceImpl implements EventService {
    final UserRepository userRepository;
    final EventDtoMapper eventDtoMapper;
    final EventRepository eventRepository;
    final UserService userService;
    final CategoryRepository categoryRepository;
    final RequestRepository requestRepository;
    final StatsClient statsClient;
    final LocationRepository locationRepository;

    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEvent) {
        User user = userService.getUserById(userId);
        Category category = categoryRepository.findById(newEvent.getCategory()).orElseThrow(
                () -> new NotFoundException("Категория отсутствует " + newEvent.getCategory())
        );

        Event event = eventDtoMapper.mapNewEventDtoToEvent(newEvent, category);
        saveLocation(event);
        event.setInitiator(user);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);
        if (event.getConfirmedRequests() == null) {
            event.setConfirmedRequests(0L);
        }
        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (LocalDateTime.now().isAfter(event.getEventDate().minus(2, ChronoUnit.HOURS))) {
            throw new WrongDataException("До начала события меньше часа, изменение невозможно");
        }
        event = eventRepository.save(event);
        log.info("Событие сохранено " + event.getId());
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        return getViewsCounter(eventFullDto);
    }

    @Override
    public List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size) {
        LocalDateTime rangeStartDateTime = null;
        LocalDateTime rangeEndDateTime = null;
        if (rangeStart != null) {
            rangeStartDateTime = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (rangeEnd != null) {
            rangeEndDateTime = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (rangeStartDateTime.isAfter(rangeEndDateTime)) {
                throw new WrongDataException("Дата и время начала поиска не должна быть позже даты и времени конца поиска");
            }
        }
        List<EventState> eventStateList;
        if (states != null) {
            eventStateList = states.stream().map(EventState::valueOf).collect(Collectors.toList());
        } else {
            eventStateList = Arrays.stream(EventState.values()).collect(Collectors.toList());
        }
        List<EventFullDto> dtos;
        if (users == null && categories == null) {
            //users = userRepository.findAll().stream().map((e) -> e.getId()).collect(Collectors.toList());
            dtos = eventRepository.findEventsByDefault(PageRequest.of(from / size, size)).stream()
                    .map(eventDtoMapper::mapEventToFullDto)
                    .collect(Collectors.toList());
        } else {
//        if (categories == null) {
//            categories = categoryRepository.findAll().stream().map((e) -> e.getId()).collect(Collectors.toList());
//        }
            dtos = eventRepository.findAllEventsWithDates(users, eventStateList, categories, rangeStartDateTime, rangeEndDateTime, PageRequest.of(from / size, size)).stream()
                    .map(eventDtoMapper::mapEventToFullDto)
                    .collect(Collectors.toList());
        }
        dtos = getViewCounters(dtos);
        return dtos;
    }

    @Override
    public EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие не существует " + eventId));

        if (LocalDateTime.now().isAfter(event.getEventDate().minus(2, ChronoUnit.HOURS))) {
            throw new ConflictException("До начала события меньше часа, изменение события невозможно");
        }
        if (!event.getState().equals(EventState.PENDING)) {
            throw new ConflictException("Событие не в состоянии \"ожидание публикации\", изменение события невозможно");
        }
        if ((!StateAction.REJECT_EVENT.toString().equals(updateRequest.getStateAction())
                && event.getState().equals(EventState.PUBLISHED))) {
            throw new ConflictException("Отклонить опубликованное событие невозможно");
        }
        updateEventWithAdminRequest(event, updateRequest);
        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new WrongDataException("Событие уже завершилось");
        }
        saveLocation(event);
        event = eventRepository.save(event);
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        return getViewsCounter(eventFullDto);
    }

    void saveLocation(Event event) {
        event.setLocation(locationRepository.save(event.getLocation()));
        log.info("Локация сохранена " + event.getLocation().getId());
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        User user = userService.getUserById(userId);
        return eventRepository.findAllByUserId(user, PageRequest.of(from / size, size)).stream()
                .map(eventDtoMapper::mapEventToShortDto)
                .collect(Collectors.toList());
    }


    @Override
    public EventFullDto getEventOfUserByIds(Long userId, Long eventId) {
        User user = userService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new WrongDataException("Пользователь " + userId + " не является инициатором события " + eventId);
        }
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        return getViewsCounter(eventFullDto);
    }

    Event getEventById(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие " + eventId + " не найдено"));
    }

    @Override
    public EventFullDto updateEventOfUserByIds(Long userId, Long eventId, UpdateEventUserRequest request) {
        User user = userService.getUserById(userId);
        Event event = getEventById(eventId);
        if (!user.getId().equals(event.getInitiator().getId())) {
            throw new WrongDataException("Пользователь " + userId + " не инициатор события " + eventId);
        }
        event = updateEventWithUserRequest(event, request);
        saveLocation(event);
        eventRepository.save(event);
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        return getViewsCounter(eventFullDto);
    }

    @Override
    public EventFullDto getEventDtoById(Long eventId, HttpServletRequest request) {
        statsClient.saveHit(new EndpointHitDto("ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        Event event = getEventById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED) && !request.getRequestURI().toLowerCase().contains("admin")) {
            throw new NotFoundException("Такого события не существует");
        }
        EventFullDto eventFullDto = eventDtoMapper.mapEventToFullDto(event);
        return getViewsCounter(eventFullDto);
    }

    Event updateEventWithUserRequest(Event event, UpdateEventUserRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory()).orElseThrow(
                    () -> new NotFoundException("Категория не существует " + updateRequest.getCategory()));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT":
                    event.setState(EventState.PUBLISHED);
                    break;
                case "REJECT_EVENT":
                case "CANCEL_REVIEW":
                    event.setState(EventState.CANCELED);
                    break;
                case "SEND_TO_REVIEW":
                    event.setState(EventState.PENDING);
                    break;
                default:
                    throw new WrongDataException("Неверное состояние события, не удалось обновить");
            }
        }
        if (LocalDateTime.now().isAfter(event.getEventDate().minus(2, ChronoUnit.HOURS))) {
            throw new WrongDataException("До начала события осталось меньше 2 часов " + event.getId());
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя изменить опубликованное событие " + event.getId());
        }
        return event;
    }

    Event updateEventWithAdminRequest(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.getAnnotation() != null) {
            event.setAnnotation(updateRequest.getAnnotation());
        }
        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory()).orElseThrow(
                    () -> new NotFoundException("Категония отсутствует " + updateRequest.getCategory()));
            event.setCategory(category);
        }
        if (updateRequest.getDescription() != null) {
            event.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(updateRequest.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if (updateRequest.getLocation() != null) {
            event.setLocation(updateRequest.getLocation());
        }
        if (updateRequest.getPaid() != null) {
            event.setPaid(updateRequest.getPaid());
        }
        if (updateRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }
        if (updateRequest.getRequestModeration() != null) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }
        if (updateRequest.getStateAction() != null) {
            switch (updateRequest.getStateAction().toUpperCase()) {
                case "PUBLISH_EVENT":
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new WrongDataException("Неверное состояние события, не удалось обновить");
            }
        }
        if (updateRequest.getTitle() != null) {
            event.setTitle(updateRequest.getTitle());
        }
        return event;
    }

    @Override
    public List<EventShortDto> getEventsWithFilters(String text,
                                                    List<Long> categories,
                                                    Boolean paid,
                                                    String rangeStart,
                                                    String rangeEnd,
                                                    Boolean onlyAvailable,
                                                    String sort,
                                                    Integer from,
                                                    Integer size,
                                                    HttpServletRequest request) {
        List<Event> events;
        LocalDateTime startDate;
        LocalDateTime endDate;
        if (rangeStart == null) {
            startDate = LocalDateTime.now();
        } else {
            startDate = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (text == null) {
            text = "";
        }
        if (categories.size() > 0 && !onlyAvailable && paid == null) {
            events = eventRepository.findAllByCategoryIdPageable(categories, PageRequest.of(from / size, size));
        } else if (rangeEnd == null) {
            events = eventRepository.findEventsByText(text.toLowerCase(), PageRequest.of(from / size, size));
        } else {
            endDate = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            if (startDate.isAfter(endDate)) {
                throw new WrongDataException("Дата и время начала поиска не должна быть позже даты и времени конца поиска");
            }
            events = eventRepository.findAllByTextAndDateRange(text.toLowerCase(),
                    startDate,
                    endDate,
                    PageRequest.of(from / size, size));
        }

        events = events.stream()
                .filter((event) -> EventState.PUBLISHED.equals(event.getState()))
                .collect(Collectors.toList());
        statsClient.saveHit(new EndpointHitDto("ewm-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        return createShortEventDtos(events);
    }

    List<EventShortDto> createShortEventDtos(List<Event> events) {
        HashMap<Long, Integer> eventIdsWithViewsCounter = new HashMap<>();
        for (Event event : events) {
            eventIdsWithViewsCounter.put(event.getId(), getViewsCounter(eventDtoMapper.mapEventToFullDto(event)).getViews());
        }
        List<ParticipationRequest> requests = requestRepository.findByEventIds(new ArrayList<>(eventIdsWithViewsCounter.keySet()));
        List<EventShortDto> dtos = events.stream()
                .map(eventDtoMapper::mapEventToShortDto)
                .collect(Collectors.toList());
        for (EventShortDto dto : dtos) {
            for (ParticipationRequest request : requests) {
                if (request.getEvent().getId().equals(dto.getId()) && request.getStatus().equals("ACCEPTED")) {
                    dto.setConfirmedRequests(dto.getConfirmedRequests() + 1);
                }
            }
            dto.setViews(eventIdsWithViewsCounter.get(dto.getId()));
        }
        return dtos;
    }

    EventFullDto getViewsCounter(EventFullDto eventFullDto) {
        Integer views = statsClient.getStats(eventFullDto.getCreatedOn(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                List.of("/events/" + eventFullDto.getId()), true).size();
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    List<EventFullDto> getViewCounters(List<EventFullDto> dtos) {
        for (EventFullDto dto : dtos) {
            getViewsCounter(dto);
        }
        return dtos;
    }
}
