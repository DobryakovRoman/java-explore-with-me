package ru.practicum.comments.service;

import org.springframework.stereotype.Component;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.event.service.EventDtoMapper;
import ru.practicum.user.service.UserDtoMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CommentDtoMapper {
    public static CommentDto mapCommentToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .event(EventDtoMapper.mapEventToShortDto(comment.getEvent()))
                .author(UserDtoMapper.mapUserToShortDto(comment.getAuthor()))
                .created(comment.getCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .state(comment.getState())
                .published(comment.getPublished() == null ?
                        null : comment.getPublished().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .updated(comment.getUpdated() == null ?
                        null : comment.getUpdated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    public static Comment mapDtoToComment(CommentDto dto) {
        return Comment.builder()
                .text(dto.getText())
                .created(LocalDateTime.now())
                .state("NEW")
                .build();
    }
}
