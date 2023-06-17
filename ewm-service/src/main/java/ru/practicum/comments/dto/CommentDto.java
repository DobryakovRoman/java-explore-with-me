package ru.practicum.comments.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {

    Long id;
    UserShortDto author;
    EventShortDto event;
    @NotBlank
    @Size(min = 5, max = 7000)
    String text;
    String created;
    String state;
    String published;
    String updated;
}
