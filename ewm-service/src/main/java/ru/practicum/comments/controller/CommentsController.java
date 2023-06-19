package ru.practicum.comments.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;
import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentsController {

    final CommentService commentService;

    @PostMapping("/users/{userId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addNewComment(@PathVariable Long userId,
                                    @RequestParam Long eventId,
                                    @Valid @RequestBody CommentDto newCommentDto) {
        log.info("Добавление комментария пользователем " + userId + " к событию " + eventId);
        return commentService.addNewComment(userId, eventId, newCommentDto);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> getCommentsByUser(@PathVariable Long userId,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение комментариев пользователя " + userId);
        return commentService.getCommentsOfUser(userId, from, size);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long commentId,
                                    @Valid @RequestBody CommentDto updateCommentDto) {
        log.info("Изменение комментария " + commentId + " пользователем " + userId);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("Удаление комментария " + commentId + " пользователем " + userId);
        commentService.deleteComment(userId, commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getCommentsOfEvent(@PathVariable Long eventId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение комментариев к событию " + eventId);
        return commentService.getCommentsOfEvent(eventId, from, size);
    }

    @GetMapping("/comments/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("Получение комментария " + commentId);
        return commentService.getCommentById(commentId);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long commentId) {
        log.info("Удаление комментария администратором " + commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @PatchMapping("/admin/comments/{commentId}/approve")
    public CommentDto approveComment(@PathVariable Long commentId) {
        log.info("approve comment {}", commentId);
        return commentService.approveComment(commentId);
    }

    @PatchMapping("/admin/comments/{commentId}/reject")
    public CommentDto rejectComment(@PathVariable Long commentId) {
        log.info("reject comment {}", commentId);
        return commentService.rejectComment(commentId);
    }
}
