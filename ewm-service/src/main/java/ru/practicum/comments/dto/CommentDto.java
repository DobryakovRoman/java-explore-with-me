package ru.practicum.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.user.dto.UserShortDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {

    Long id;
    UserShortDto author;
    EventShortDto event;
    @NotBlank
    @Size(min = 5, max = 7000)
    String text;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime created;
    String state;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime published;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updated;
}
