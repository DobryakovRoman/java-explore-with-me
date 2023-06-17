package ru.practicum.comments.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Event;
import ru.practicum.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "text", nullable = false, length = 7000)
    String text;

    @ManyToOne
    Event event;

    @ManyToOne
    User author;

    @Column(nullable = false)
    LocalDateTime created;

    String state;

    LocalDateTime published;

    LocalDateTime updated;
}
