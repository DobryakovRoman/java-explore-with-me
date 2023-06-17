package ru.practicum.request.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParticipationRequestDto {

    String created;
    Long event;
    Long id;
    Long requester;
    String status;
}
