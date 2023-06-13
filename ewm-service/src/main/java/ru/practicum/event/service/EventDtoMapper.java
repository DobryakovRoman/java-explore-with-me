package ru.practicum.event.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryDtoMapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.service.UserDtoMapper;

import java.time.format.DateTimeFormatter;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventDtoMapper {
    final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    public EventFullDto mapEventToFullDto(Event event) {
        if (event.getState() == null) {
            event.setState(EventState.PENDING);
        }
        if (event.getConfirmedRequests() == null) {
            event.setConfirmedRequests(0L);
        }
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(CategoryDtoMapper.mapCategoryToDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn().format(DateTimeFormatter.ofPattern(dateTimeFormat)))
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(UserDtoMapper.mapUserToShortDto(event.getInitiator()))
                .location(event.getLocation())
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(checkPublishedOn(event))
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .build();
    }

    public EventShortDto mapEventToShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .title(event.getTitle())
                .category(CategoryDtoMapper.mapCategoryToDto(event.getCategory()))
                .initiator(UserDtoMapper.mapUserToShortDto(event.getInitiator()))
                .id(event.getId())
                .paid(event.getPaid())
                .eventDate(event.getEventDate())
                .build();
    }

    public Event mapNewEventDtoToEvent(NewEventDto newEvent, Category category) {
        return Event.builder()
                .annotation(newEvent.getAnnotation())
                .category(category)
                .description(newEvent.getDescription())
                .eventDate(newEvent.getEventDate())
                .location(newEvent.getLocation())
                .paid(newEvent.getPaid())
                .participantLimit(newEvent.getParticipantLimit())
                .requestModeration(newEvent.getRequestModeration())
                .title(newEvent.getTitle())
                .build();
    }

    String checkPublishedOn(Event event) {
        if (event.getPublishedOn() == null) {
            return null;
        }
        return event.getPublishedOn().format(DateTimeFormatter.ofPattern(dateTimeFormat));
    }
}
