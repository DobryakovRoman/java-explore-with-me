package ru.practicum.event.service;

import ru.practicum.event.dto.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface EventService {
    List<EventFullDto> getEvents(List<Long> users, List<String> states, List<Long> categories, String rangeStart, String rangeEnd, Integer from, Integer size);

    EventFullDto updateEvent(Long eventId, UpdateEventAdminRequest updateRequest);

    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    EventFullDto addEvent(Long userId, NewEventDto newEvent);

    EventFullDto getEventDtoById(Long eventId, HttpServletRequest request);

    EventFullDto getEventOfUserByIds(Long userId, Long eventId);

    EventFullDto updateEventOfUserByIds(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventShortDto> getEventsWithFilters(String text,
                                             List<Integer> categories,
                                             Boolean paid,
                                             String rangeStart,
                                             String rangeEnd,
                                             Boolean onlyAvailable,
                                             String sort,
                                             Integer from,
                                             Integer size,
                                             HttpServletRequest request);
}
