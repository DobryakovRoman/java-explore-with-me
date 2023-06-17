package ru.practicum.event.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.dto.*;
import ru.practicum.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RestController
public class EventController {

    final EventService eventService;

    @GetMapping("/admin/events")
    public List<EventFullDto> getEvents(@RequestParam(required = false) List<Long> users,
                                        @RequestParam(required = false) List<String> states,
                                        @RequestParam(required = false) List<Long> categories,
                                        @RequestParam(required = false) String rangeStart,
                                        @RequestParam(required = false) String rangeEnd,
                                        @RequestParam(required = false, defaultValue = "0") Integer from,
                                        @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Поиск событий");
        return eventService.getEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Редактирование данных события и его статуса");
        return eventService.updateEvent(eventId, updateEventAdminRequest);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEventById(@PathVariable Long id,
                                     HttpServletRequest request) {
        log.info("Получение подробной информации об опубликованном событии по его идентификатору");
        return eventService.getEventDtoById(id, request.getRequestURI(), request.getRemoteAddr());
    }

    @GetMapping("/events")
    public List<EventShortDto> getEventsWithFilters(@RequestParam(required = false) String text,
                                                    @RequestParam(required = false) List<Long> categories,
                                                    @RequestParam(required = false) Boolean paid,
                                                    @RequestParam(required = false) String rangeStart,
                                                    @RequestParam(required = false) String rangeEnd,
                                                    @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
                                                    @RequestParam(required = false, defaultValue = "EVENT_DATE") String sort,
                                                    @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                    @Positive @RequestParam(defaultValue = "10") Integer size,
                                                    HttpServletRequest request) {
        log.info("Получение событий с возможностью фильтрации");
        return eventService.getEventsWithFilters(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size,
                request.getRequestURI(), request.getRemoteAddr());
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getEventsByUser(@PathVariable Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение событий, добавленных пользователем " + userId);
        return eventService.getEventsByUser(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addNewEventByUser(@PathVariable Long userId,
                                          @Valid @RequestBody NewEventDto newEvent) {
        log.info("Добавление нового события пользователем " + userId);
        return eventService.addEvent(userId, newEvent);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getEventOfUserByIds(@PathVariable Long userId,
                                            @PathVariable Long eventId) {
        log.info("Получение полной информации о событии " + eventId + " пользователем " + userId);
        return eventService.getEventOfUserByIds(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateEventOfUserByIds(@PathVariable Long userId,
                                               @PathVariable Long eventId,
                                               @Valid @RequestBody UpdateEventUserRequest request) {
        log.info("Изменение события " + eventId + " пользователем " + userId);
        return eventService.updateEventOfUserByIds(userId, eventId, request);
    }

}
