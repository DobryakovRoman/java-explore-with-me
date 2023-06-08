package ru.practicum.event.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Location;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;
    Long category;

    @NotBlank
    @Size(min = 20)
    String description;
    String eventDate;
    Location location;

    Boolean paid;
    Integer participantLimit;
    Boolean requestModeration;

    @Size(min = 3, max = 120)
    String title;
}
