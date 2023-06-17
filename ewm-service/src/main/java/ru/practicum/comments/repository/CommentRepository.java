package ru.practicum.comments.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.comments.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT comment FROM Comment comment " +
            "WHERE comment.event.id = :eventId " +
            "AND comment.state = :state " +
            "ORDER BY comment.created DESC")
    List<Comment> findAllByEventIdAndState(Long eventId, String state, Pageable page);

    @Query("SELECT comment FROM Comment comment " +
            "WHERE comment.author.id = :userId " +
            "ORDER BY comment.created DESC")
    List<Comment> findAllByUserId(Long userId, Pageable page);
}
